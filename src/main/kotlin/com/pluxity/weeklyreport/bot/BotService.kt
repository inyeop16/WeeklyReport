package com.pluxity.weeklyreport.bot

import com.pluxity.weeklyreport.bot.dto.Activity
import com.pluxity.weeklyreport.bot.dto.Attachment
import com.pluxity.weeklyreport.domain.repository.UserRepository
import com.pluxity.weeklyreport.dto.request.CreateDailyEntryRequest
import com.pluxity.weeklyreport.dto.request.GenerateReportRequest
import com.pluxity.weeklyreport.dto.request.UpdateReportRequest
import com.pluxity.weeklyreport.service.DailyEntryService
import com.pluxity.weeklyreport.service.ReportService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Service
class BotService(
    private val userRepository: UserRepository,
    private val dailyEntryService: DailyEntryService,
    private val reportService: ReportService,
    private val cardBuilder: AdaptiveCardBuilder
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun handleMessage(activity: Activity): Activity {
        // Adaptive Card 버튼 클릭 (value가 있으면 카드 액션)
        if (activity.value != null) {
            return handleCardAction(activity)
        }

        val text = activity.text?.trim() ?: return replyText(activity, "메시지를 입력해주세요.")

        return when {
            text.contains("@주간보고") ->
                handleGenerateReport(activity)

            text.contains("@일일보고") ->
                handleDailyEntry(activity, text)

            text.contains("@회의실") ->
                replyCard(activity, cardBuilder.buildRoomCard())

            text.contains("@청소당번") ->
                replyCard(activity, cardBuilder.buildCleanCard())
            
            else
                ->  replyCard(activity, cardBuilder.buildHelpCard())
        }
    }

    private fun handleDailyEntry(activity: Activity, text: String): Activity {

        val entry = dailyEntryService.create(
            CreateDailyEntryRequest(
                entryDate = LocalDate.now(),
                content = text
            )
        )

        return replyCard(activity, cardBuilder.buildEntryConfirmCard(entry))
    }

    private fun handleGenerateReport(activity: Activity): Activity {
        val user = findOrCreateUser(activity)
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))

        return try {
            val report = reportService.generate(
                GenerateReportRequest(
                    weekStart = weekStart,
                    weekEnd = weekEnd
                )
            )
            replyCard(activity, cardBuilder.buildReportCard(report))
        } catch (e: Exception) {
            log.error("보고서 생성 실패", e)
            replyText(activity, "보고서 생성 실패: ${e.message}")
        }
    }

    private fun handleCardAction(activity: Activity): Activity {
        val data = activity.value ?: return replyText(activity, "잘못된 요청입니다.")
        val action = data["action"] as? String ?: return replyText(activity, "알 수 없는 액션입니다.")
        val reportId = (data["reportId"] as? Number)?.toLong()
            ?: return replyText(activity, "보고서 ID가 없습니다.")

        return when (action) {
            "edit_report" -> {
                val instruction = data["instruction"] as? String
                    ?: return replyText(activity, "수정 지시사항을 입력해주세요.")

                try {
                    val report = reportService.update(reportId, UpdateReportRequest(instruction))
                    replyCard(activity, cardBuilder.buildReportCard(report))
                } catch (e: Exception) {
                    log.error("보고서 수정 실패", e)
                    replyText(activity, "보고서 수정 실패: ${e.message}")
                }
            }

            "send_report" -> {
                replyText(activity, "보고서(#$reportId) 전송 기능은 웹 API를 통해 수행해주세요.\nPOST /api/reports/send")
            }

            else -> replyText(activity, "알 수 없는 액션: $action")
        }
    }

    private fun findOrCreateUser(activity: Activity): com.pluxity.weeklyreport.domain.entity.User {
        val fromName = activity.from?.name ?: "Unknown"
        val fromId = activity.from?.id ?: "unknown"

        // Teams에서 오는 경우 from.id 기반으로 사용자 조회/생성
        val email = "$fromId@bot.local"

        return userRepository.findByEmail(email)
            ?: userRepository.save(
                com.pluxity.weeklyreport.domain.entity.User(
                    username = fromId,
                    name = fromName,
                    email = email,
                    password = ""
                )
            )
    }

    private fun replyText(incoming: Activity, text: String) = Activity(
        type = "message",
        text = text,
        from = incoming.recipient,
        recipient = incoming.from,
        conversation = incoming.conversation,
        replyToId = incoming.id
    )

    private fun replyCard(incoming: Activity, card: Map<String, Any>) = Activity(
        type = "message",
        from = incoming.recipient,
        recipient = incoming.from,
        conversation = incoming.conversation,
        replyToId = incoming.id,
        attachments = listOf(
            Attachment(
                contentType = "application/vnd.microsoft.card.adaptive",
                content = card
            )
        )
    )
}
