package com.pluxity.weeklyreport.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.pluxity.weeklyreport.ai.AiAdapter
import com.pluxity.weeklyreport.ai.dto.AiRequest
import com.pluxity.weeklyreport.domain.entity.Report
import com.pluxity.weeklyreport.domain.entity.ReportTask
import com.pluxity.weeklyreport.domain.entity.TaskStatus
import com.pluxity.weeklyreport.domain.entity.User
import com.pluxity.weeklyreport.domain.repository.DailyEntryRepository
import com.pluxity.weeklyreport.domain.repository.ReportRepository
import com.pluxity.weeklyreport.domain.repository.TemplateRepository
import com.pluxity.weeklyreport.domain.repository.UserRepository
import com.pluxity.weeklyreport.dto.AiTaskDto
import com.pluxity.weeklyreport.dto.AiTaskListDto
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

    private data class GeneratedContent(
        val rendered: String,
        val rawEntriesJson: String,
        val parsedTasks: List<AiTaskDto>
    )

    @Transactional
    fun getOrGenerate(request: GenerateReportRequest, userId: Long): ReportResponse {
        val existing = reportRepository.findByUserIdAndWeekStartAndWeekEndAndIsLastTrue(
            userId, request.weekStart, request.weekEnd
        )
        if (existing != null) return existing.toResponse()

        val user = userService.getEntity(userId)
        val content = generateContent(user, request.weekStart, request.weekEnd)

        return createAndSaveNewReport(user, request.weekStart, request.weekEnd, content)
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
            existing.tasks.clear()
            addTasksToReport(existing, content.parsedTasks)
            return reportRepository.save(existing).toResponse()
        }

        return createAndSaveNewReport(user, request.weekStart, request.weekEnd, content)
    }

    private fun createAndSaveNewReport(user: User, weekStart: LocalDate, weekEnd: LocalDate, content: GeneratedContent): ReportResponse {
        val report = Report(
            user = user,
            weekStart = weekStart,
            weekEnd = weekEnd,
            rendered = content.rendered,
            rawEntries = content.rawEntriesJson,
            isLast = true
        )
        addTasksToReport(report, content.parsedTasks)
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

        val jsonContent = extractJson(aiResponse.content)
        val aiTaskList = try {
            objectMapper.readValue(jsonContent, AiTaskListDto::class.java)
        } catch (_: Exception) {
            throw BusinessException("AI 응답 파싱에 실패했습니다. 다시 시도해 주세요.")
        }

        val rendered = renderFromTasks(user, weekStart, weekEnd, aiTaskList.task)
        return GeneratedContent(rendered, rawEntriesJson, aiTaskList.task)
    }

    private fun extractJson(aiContent: String): String {
        val trimmed = aiContent.trim()
        val jsonPattern = Regex("""```(?:json)?\s*\n?(.*?)\n?\s*```""", RegexOption.DOT_MATCHES_ALL)
        val match = jsonPattern.find(trimmed)
        return match?.groupValues?.get(1)?.trim() ?: trimmed
    }

    private fun renderFromTasks(
        user: User,
        weekStart: LocalDate,
        weekEnd: LocalDate,
        tasks: List<AiTaskDto>
    ): String {
        val sb = StringBuilder()

        // 헤더 섹션
        sb.appendLine("주간업무보고")
        sb.appendLine("보고 기간: $weekStart ~ $weekEnd")
        sb.appendLine("작성자: ${user.name}")
        sb.appendLine()

        // 1. 금주 실적 (DONE 상태인 업무들)
        val doneTasks = tasks.filter { it.status == "DONE" }
        sb.appendLine("■ 금주 실적")
        if (doneTasks.isEmpty()) {
            sb.appendLine(" • 없음")
        } else {
            doneTasks.groupBy { it.project ?: "기타" }.forEach { (project, projectTasks) ->
                projectTasks.forEach { task ->
                    sb.appendLine(" • ($project) ${task.description}")
                }
            }
        }
        sb.appendLine()

        // 2. 진행 중 업무 (IN_PROGRESS, TODO 또는 null인 업무들)
        val ongoingTasks = tasks.filter { it.status != "DONE" }
        sb.appendLine("■ 진행 중 업무")
        if (ongoingTasks.isEmpty()) {
            sb.appendLine(" • 없음")
        } else {
            ongoingTasks.groupBy { it.project ?: "기타" }.forEach { (project, projectTasks) ->
                projectTasks.forEach { task ->
                    val statusLabel = when (task.status) {
                        "IN_PROGRESS" -> "진행 중"
                        "TODO" -> "예정"
                        else -> "상태 미정"
                    }
                    val progressText = task.progress?.let { " - $it%" } ?: " - $statusLabel"
                    sb.appendLine(" • $project — ${task.description}$progressText")
                }
            }
        }

        return sb.toString()
    }

    private fun addTasksToReport(report: Report, parsedTasks: List<AiTaskDto>) {
        parsedTasks.forEach { dto ->
            val task = ReportTask(
                report = report,
                project = dto.project,
                description = dto.description,
                status = dto.status?.let { runCatching { TaskStatus.valueOf(it) }.getOrNull() },
                progress = dto.progress,
            )
            report.tasks.add(task)
        }
    }
}
