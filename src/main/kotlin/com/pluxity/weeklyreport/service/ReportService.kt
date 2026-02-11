package com.pluxity.weeklyreport.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.pluxity.weeklyreport.ai.AiAdapter
import com.pluxity.weeklyreport.ai.dto.AiRequest
import com.pluxity.weeklyreport.domain.entity.Report
import com.pluxity.weeklyreport.domain.repository.DailyEntryRepository
import com.pluxity.weeklyreport.domain.repository.ReportRepository
import com.pluxity.weeklyreport.dto.request.GenerateReportRequest
import com.pluxity.weeklyreport.dto.request.SendReportRequest
import com.pluxity.weeklyreport.dto.request.UpdateReportRequest
import com.pluxity.weeklyreport.dto.response.ReportResponse
import com.pluxity.weeklyreport.exception.BusinessException
import com.pluxity.weeklyreport.exception.ResourceNotFoundException
import com.pluxity.weeklyreport.notification.NotificationAdapter
import com.pluxity.weeklyreport.notification.dto.NotificationRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    fun generate(request: GenerateReportRequest): ReportResponse {
        val user = userService.getEntity(request.userId)
        val template = templateService.getEntity(request.templateId)

        val entries = dailyEntryRepository.findByUserIdAndEntryDateBetween(
            request.userId, request.weekStart, request.weekEnd
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
            systemPrompt = template.systemPrompt,
            userMessage = """
                사용자: ${user.name}
                부서: ${user.department ?: "미지정"}
                기간: ${request.weekStart} ~ ${request.weekEnd}

                업무 기록:
                $rawEntriesJson
            """.trimIndent()
        )

        val aiResponse = aiAdapter.generate(aiRequest)

        val report = Report(
            user = user,
            weekStart = request.weekStart,
            weekEnd = request.weekEnd,
            rendered = aiResponse.content,
            rawEntries = rawEntriesJson
        )

        return ReportResponse.from(reportRepository.save(report))
    }

    @Transactional
    fun update(id: Long, request: UpdateReportRequest): ReportResponse {
        val report = reportRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Report", "id", id) }

        if (report.rendered.isNullOrBlank()) {
            throw BusinessException("수정할 보고서 내용이 없습니다")
        }

        val aiRequest = AiRequest(
            systemPrompt = """당신은 주간보고서 편집 도우미입니다.
                |사용자가 기존 보고서와 수정 지시사항을 제공하면, 지시에 따라 보고서를 수정해서 전체 보고서를 다시 작성하세요.
                |수정된 보고서 본문만 출력하세요. 다른 설명은 붙이지 마세요.""".trimMargin(),
            userMessage = """기존 보고서:
                |${report.rendered}
                |
                |수정 지시사항:
                |${request.instruction}""".trimMargin()
        )

        val aiResponse = aiAdapter.generate(aiRequest)
        report.rendered = aiResponse.content

        return ReportResponse.from(reportRepository.save(report))
    }

    @Transactional
    fun send(request: SendReportRequest): ReportResponse {
        val report = reportRepository.findById(request.reportId)
            .orElseThrow { ResourceNotFoundException("Report", "id", request.reportId) }

        if (report.rendered.isNullOrBlank()) {
            throw BusinessException("전송할 보고서 내용이 없습니다")
        }

        val notificationRequest = NotificationRequest(
            subject = "주간보고 (${report.weekStart} ~ ${report.weekEnd})",
            body = report.rendered!!,
            recipients = request.recipients
        )

        notificationAdapter.send(notificationRequest)

        val existingSentTo = report.sentTo?.toMutableList() ?: mutableListOf()
        existingSentTo.addAll(request.recipients)
        report.sentTo = existingSentTo.distinct().toTypedArray()

        return ReportResponse.from(reportRepository.save(report))
    }

    fun findById(id: Long): ReportResponse {
        val report = reportRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Report", "id", id) }
        return ReportResponse.from(report)
    }

    fun findByUserId(userId: Long): List<ReportResponse> =
        reportRepository.findByUserId(userId).map(ReportResponse::from)
}
