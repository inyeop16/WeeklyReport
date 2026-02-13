package com.pluxity.weeklyreport.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.pluxity.weeklyreport.ai.AiAdapter
import com.pluxity.weeklyreport.ai.dto.AiRequest
import com.pluxity.weeklyreport.domain.entity.Report
import com.pluxity.weeklyreport.domain.entity.User
import com.pluxity.weeklyreport.domain.repository.DailyEntryRepository
import com.pluxity.weeklyreport.domain.repository.ReportRepository
import com.pluxity.weeklyreport.dto.request.GenerateReportRequest
import com.pluxity.weeklyreport.dto.request.ModifyReportRequest
import com.pluxity.weeklyreport.dto.request.SendReportRequest
import com.pluxity.weeklyreport.dto.response.ReportResponse
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
    private val userService: UserService,
    private val templateService: TemplateService,
    private val aiAdapter: AiAdapter,
    private val notificationAdapter: NotificationAdapter,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun getOrGenerate(request: GenerateReportRequest, userId: Long): ReportResponse {
        val existing = reportRepository.findByUserIdAndWeekStartAndWeekEndAndIsLastTrue(
            userId, request.weekStart, request.weekEnd
        )
        if (existing != null) return existing.toResponse()

        val user = userService.getEntity(userId)
        return generateNewReport(user, request.weekStart, request.weekEnd).toResponse()
    }

    @Transactional
    fun regenerate(request: GenerateReportRequest, userId: Long): ReportResponse {
        val user = userService.getEntity(userId)
        invalidateCurrentVersion(userId, request.weekStart, request.weekEnd)
        return generateNewReport(user, request.weekStart, request.weekEnd).toResponse()
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

        current.isLast = false
        reportRepository.save(current)

        val newReport = Report(
            user = current.user,
            weekStart = request.weekStart,
            weekEnd = request.weekEnd,
            rendered = aiResponse.content,
            rawEntries = current.rawEntries,
            isLast = true
        )
        return reportRepository.save(newReport).toResponse()
    }

    fun getVersions(userId: Long, weekStart: LocalDate, weekEnd: LocalDate): List<ReportResponse> =
        reportRepository.findByUserIdAndWeekStartAndWeekEndOrderByCreatedAtDesc(
            userId, weekStart, weekEnd
        ).map { it.toResponse() }

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

        // 선택한 버전을 current로 설정
        invalidateCurrentVersion(userId, report.weekStart, report.weekEnd)
        report.isLast = true

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

    private fun generateNewReport(user: User, weekStart: LocalDate, weekEnd: LocalDate): Report {
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

        val report = Report(
            user = user,
            weekStart = weekStart,
            weekEnd = weekEnd,
            rendered = aiResponse.content,
            rawEntries = rawEntriesJson,
            isLast = true
        )

        return reportRepository.save(report)
    }

    private fun invalidateCurrentVersion(userId: Long, weekStart: LocalDate, weekEnd: LocalDate) {
        reportRepository.findByUserIdAndWeekStartAndWeekEndAndIsLastTrue(userId, weekStart, weekEnd)?.let { currentReport ->
            currentReport.isLast = false
            reportRepository.save(currentReport)
        }
    }
}
