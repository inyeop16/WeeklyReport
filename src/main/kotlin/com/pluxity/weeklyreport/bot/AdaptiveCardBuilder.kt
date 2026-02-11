package com.pluxity.weeklyreport.bot

import com.pluxity.weeklyreport.dto.response.DailyEntryResponse
import com.pluxity.weeklyreport.dto.response.ReportResponse
import org.springframework.stereotype.Component

@Component
class AdaptiveCardBuilder {

    fun buildReportCard(report: ReportResponse): Map<String, Any> = mapOf(
        "type" to "AdaptiveCard",
        "version" to "1.4",
        "\$schema" to "http://adaptivecards.io/schemas/adaptive-card.json",
        "body" to listOf(
            mapOf(
                "type" to "TextBlock",
                "text" to "주간보고서 (${report.weekStart} ~ ${report.weekEnd})",
                "weight" to "bolder",
                "size" to "medium"
            ),
            mapOf(
                "type" to "TextBlock",
                "text" to (report.rendered ?: "내용 없음"),
                "wrap" to true
            )
        ),
        "actions" to listOf(
            mapOf(
                "type" to "Action.Submit",
                "title" to "전송",
                "data" to mapOf("action" to "send_report", "reportId" to report.id)
            ),
            mapOf(
                "type" to "Action.ShowCard",
                "title" to "수정",
                "card" to mapOf(
                    "type" to "AdaptiveCard",
                    "body" to listOf(
                        mapOf(
                            "type" to "Input.Text",
                            "id" to "instruction",
                            "placeholder" to "수정 지시사항을 입력하세요",
                            "isMultiline" to true
                        )
                    ),
                    "actions" to listOf(
                        mapOf(
                            "type" to "Action.Submit",
                            "title" to "수정 요청",
                            "data" to mapOf("action" to "edit_report", "reportId" to report.id)
                        )
                    )
                )
            )
        )
    )

    fun buildEntryConfirmCard(entry: DailyEntryResponse): Map<String, Any> = mapOf(
        "type" to "AdaptiveCard",
        "version" to "1.4",
        "\$schema" to "http://adaptivecards.io/schemas/adaptive-card.json",
        "body" to listOf(
            mapOf(
                "type" to "TextBlock",
                "text" to "업무 기록 저장 완료",
                "weight" to "bolder"
            ),
            mapOf(
                "type" to "FactSet",
                "facts" to listOf(
                    mapOf("title" to "날짜", "value" to entry.entryDate.toString()),
                    mapOf("title" to "내용", "value" to entry.content)
                )
            )
        )
    )

    fun buildHelpCard(): Map<String, Any> = mapOf(
        "type" to "AdaptiveCard",
        "version" to "1.4",
        "\$schema" to "http://adaptivecards.io/schemas/adaptive-card.json",
        "body" to listOf(
            mapOf(
                "type" to "TextBlock",
                "text" to "주간보고 봇 사용법",
                "weight" to "bolder",
                "size" to "medium"
            ),
            mapOf(
                "type" to "TextBlock",
                "text" to """**업무 기록**: 텍스트를 그냥 입력하면 오늘 날짜로 저장됩니다.
                    |
                    |**보고서 생성**: "@주간보고" 라고 입력하세요.
                    |
                    |**일일보고서 저장**: "@일일보고" 라고 입력하세요.""".trimMargin(),
                "wrap" to true
            )
        )
    )

    fun buildRoomCard(): Map<String, Any> = mapOf(
        "type" to "AdaptiveCard",
        "version" to "1.4",
        "\$schema" to "http://adaptivecards.io/schemas/adaptive-card.json",
        "body" to listOf(
            mapOf(
                "type" to "TextBlock",
                "text" to "회의실 예약",
                "weight" to "bolder",
                "size" to "medium"
            ),
            mapOf(
                "type" to "TextBlock",
                "text" to """ 회의실이 예약되었습니다.""".trimMargin(),
                "wrap" to true
            )
        )
    )

    fun buildCleanCard(): Map<String, Any> = mapOf(
        "type" to "AdaptiveCard",
        "version" to "1.4",
        "\$schema" to "http://adaptivecards.io/schemas/adaptive-card.json",
        "body" to listOf(
            mapOf(
                "type" to "TextBlock",
                "text" to "이번주 분리수거 담당팀",
                "weight" to "bolder",
                "size" to "medium"
            ),
            mapOf(
                "type" to "TextBlock",
                "text" to """ 플랫폼 개발팀""".trimMargin(),
                "wrap" to true
            )
        )
    )
}
