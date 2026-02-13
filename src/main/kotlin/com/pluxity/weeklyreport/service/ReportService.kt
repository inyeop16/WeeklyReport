package com.pluxity.weeklyreport.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.pluxity.weeklyreport.ai.AiAdapter
import com.pluxity.weeklyreport.ai.dto.AiRequest
import com.pluxity.weeklyreport.domain.entity.Report
import com.pluxity.weeklyreport.domain.entity.User
import com.pluxity.weeklyreport.domain.repository.DailyEntryRepository
import com.pluxity.weeklyreport.domain.repository.ReportRepository
import com.pluxity.weeklyreport.domain.repository.TemplateRepository
import com.pluxity.weeklyreport.domain.repository.UserRepository
import com.pluxity.weeklyreport.dto.request.GenerateReportRequest
import com.pluxity.weeklyreport.dto.request.ModifyReportRequest
import com.pluxity.weeklyreport.dto.request.SelectCandidateRequest
import com.pluxity.weeklyreport.dto.request.SendReportRequest
import com.pluxity.weeklyreport.dto.response.ReportResponse
import com.pluxity.weeklyreport.dto.request.GenerateTeamReportRequest
import com.pluxity.weeklyreport.dto.response.toResponse
import com.pluxity.weeklyreport.exception.BusinessException
import com.pluxity.weeklyreport.exception.ResourceNotFoundException
import com.pluxity.weeklyreport.notification.NotificationAdapter
import com.pluxity.weeklyreport.notification.dto.NotificationRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class ReportService(
    private val reportRepository: ReportRepository,
    private val dailyEntryRepository: DailyEntryRepository,
    private val userRepository: UserRepository,
    private val templateRepository: TemplateRepository,
    private val userService: UserService,
    private val templateService: TemplateService,
    private val departmentService: DepartmentService,
    private val aiAdapter: AiAdapter,
    private val notificationAdapter: NotificationAdapter,
    private val objectMapper: ObjectMapper
) {

    private data class GeneratedContent(val rendered: String, val rawEntriesJson: String)

    @Transactional
    fun getOrGenerate(request: GenerateReportRequest, userId: Long): ReportResponse {
        val existing = reportRepository.findByUserIdAndWeekStartAndWeekEndAndIsLastTrue(
            userId, request.weekStart, request.weekEnd
        )
        if (existing != null) return existing.toResponse()

        val user = userService.getEntity(userId)
        val content = generateContent(user, request.weekStart, request.weekEnd)

        val report = Report(
            user = user,
            weekStart = request.weekStart,
            weekEnd = request.weekEnd,
            rendered = content.rendered,
            rawEntries = content.rawEntriesJson,
            isLast = true
        )
        return reportRepository.save(report).toResponse()
    }

    @Transactional
    fun regenerate(request: GenerateReportRequest, userId: Long): ReportResponse {
        val user = userService.getEntity(userId)
        val existing = reportRepository.findByUserIdAndWeekStartAndWeekEndAndIsLastTrue(
            userId, request.weekStart, request.weekEnd
        )

        val content = generateContent(user, request.weekStart, request.weekEnd)

        if (existing != null) {
            existing.candidateRendered = content.rendered
            existing.rawEntries = content.rawEntriesJson
            return reportRepository.save(existing).toResponse()
        }

        val report = Report(
            user = user,
            weekStart = request.weekStart,
            weekEnd = request.weekEnd,
            rendered = content.rendered,
            rawEntries = content.rawEntriesJson,
            isLast = true
        )
        return reportRepository.save(report).toResponse()
    }

    @Transactional
    fun generateTeam(request: GenerateTeamReportRequest, userId: Long): ReportResponse {
        val requestUser = userService.getEntity(userId)
        val department = departmentService.getEntity(request.departmentId)
        val members = userRepository.findByDepartmentId(request.departmentId)

        if (members.isEmpty()) {
            throw BusinessException("해당 부서에 소속된 사용자가 없습니다")
        }

        val teamEntriesData = members.mapNotNull { member ->
            val entries = dailyEntryRepository.findByUserIdAndEntryDateBetween(
                member.id, request.weekStart, request.weekEnd
            )
            if (entries.isEmpty()) return@mapNotNull null
            mapOf(
                "name" to member.name,
                "entries" to entries.map { entry ->
                    mapOf(
                        "date" to entry.entryDate.toString(),
                        "content" to entry.content,
                        "category" to entry.category
                    )
                }
            )
        }

        if (teamEntriesData.isEmpty()) {
            throw BusinessException("해당 기간에 등록된 팀원 업무 기록이 없습니다")
        }

        val rawEntriesJson = objectMapper.writeValueAsString(teamEntriesData)

        val systemPrompt = if (request.templateId != null) {
            val template = templateRepository.findById(request.templateId)
                .orElseThrow { ResourceNotFoundException("Template", "id", request.templateId) }
            template.systemPrompt
        } else {
            ""
        }

        val aiRequest = AiRequest(
            systemPrompt = systemPrompt,
            userMessage = """
                부서: ${department.name}
                기간: ${request.weekStart} ~ ${request.weekEnd}
                팀원 수: ${teamEntriesData.size}명

                팀원별 업무 기록:
                $rawEntriesJson
            """.trimIndent()
        )

        val aiResponse = aiAdapter.generate(aiRequest)

        val report = Report(
            user = requestUser,
            weekStart = request.weekStart,
            weekEnd = request.weekEnd,
            rendered = aiResponse.content,
            rawEntries = rawEntriesJson
        )

        return reportRepository.save(report).toResponse()
    }

    @Transactional
    fun modify(request: ModifyReportRequest, userId: Long): ReportResponse {
        val current = reportRepository.findByUserIdAndWeekStartAndWeekEndAndIsLastTrue(
            userId, request.weekStart, request.weekEnd
        ) ?: throw BusinessException("수정할 보고서가 없습니다. 먼저 보고서를 생성하세요.")

        if (current.rendered.isNullOrBlank()) {
            throw BusinessException("수정할 보고서 내용이 없습니다")
        }

        val aiRequest = AiRequest(
            systemPrompt = """당신은 주간보고서 편집 도우미입니다.
                |사용자가 기존 보고서와 수정 지시사항을 제공하면, 지시에 따라 보고서를 수정해서 전체 보고서를 다시 작성하세요.
                |수정된 보고서 본문만 출력하세요. 다른 설명은 붙이지 마세요. 기존 보고서 형식은 지켜야합니다.""".trimMargin(),
            userMessage = """기존 보고서:
                |${current.rendered}
                |
                |수정 지시사항:
                |${request.instruction}""".trimMargin()
        )

        val aiResponse = aiAdapter.generate(aiRequest)

        current.candidateRendered = aiResponse.content
        return reportRepository.save(current).toResponse()
    }

    @Transactional
    fun selectCandidate(request: SelectCandidateRequest, userId: Long): ReportResponse {
        val report = reportRepository.findByUserIdAndWeekStartAndWeekEndAndIsLastTrue(
            userId, request.weekStart, request.weekEnd
        ) ?: throw BusinessException("보고서가 없습니다")

        if (report.candidateRendered == null) {
            throw BusinessException("선택할 후보 보고서가 없습니다")
        }

        if (request.acceptCandidate) {
            report.rendered = report.candidateRendered
        }
        report.candidateRendered = null

        return reportRepository.save(report).toResponse()
    }

    @Transactional
    fun send(request: SendReportRequest, userId: Long): ReportResponse {
        val report = reportRepository.findById(request.reportId)
            .orElseThrow { ResourceNotFoundException("Report", "id", request.reportId) }

        if (report.user.id != userId) {
            throw BusinessException("본인의 보고서만 전송할 수 있습니다")
        }

        if (report.rendered.isNullOrBlank()) {
            throw BusinessException("전송할 보고서 내용이 없습니다")
        }

        if (report.candidateRendered != null) {
            throw BusinessException("후보 보고서가 있습니다. 먼저 후보를 선택하세요.")
        }

        val notificationRequest = NotificationRequest(
            subject = "주간보고 (${report.weekStart} ~ ${report.weekEnd})",
            body = report.rendered!!,
            recipients = emptyList()
        )

        notificationAdapter.send(notificationRequest)
        report.isSent = true

        return reportRepository.save(report).toResponse()
    }

    fun findById(id: Long): ReportResponse {
        val report = reportRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Report", "id", id) }
        return report.toResponse()
    }

    fun findByUserId(userId: Long): List<ReportResponse> =
        reportRepository.findByUserIdAndIsLastTrue(userId).map { it.toResponse() }

    private fun generateContent(user: User, weekStart: LocalDate, weekEnd: LocalDate): GeneratedContent {
        val template = templateService.findActive(user.department?.name)

        if (template.isEmpty()) throw BusinessException("등록된 템플릿이 없습니다.")

        val entries = dailyEntryRepository.findByUserIdAndEntryDateBetween(
            user.id, weekStart, weekEnd
        )

        if (entries.isEmpty()) {
            throw BusinessException("해당 기간에 등록된 업무 기록이 없습니다")
        }

        val entriesData = entries.map { entry ->
            mapOf(
                "date" to entry.entryDate.toString(),
                "content" to entry.content,
                "category" to entry.category
            )
        }
        val rawEntriesJson = objectMapper.writeValueAsString(entriesData)

        val aiRequest = AiRequest(
            systemPrompt = template.first().systemPrompt,
            userMessage = """
                사용자: ${user.name}
                부서: ${user.department?.name ?: "미지정"}
                기간: ${weekStart} ~ ${weekEnd}

                업무 기록:
                $rawEntriesJson
            """.trimIndent()
        )

        val aiResponse = aiAdapter.generate(aiRequest)
        return GeneratedContent(aiResponse.content, rawEntriesJson)
    }
}
