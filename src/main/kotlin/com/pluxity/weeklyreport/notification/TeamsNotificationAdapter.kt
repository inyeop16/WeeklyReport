package com.pluxity.weeklyreport.notification

import com.pluxity.weeklyreport.notification.dto.NotificationRequest
import org.springframework.web.client.RestClient

class TeamsNotificationAdapter(
    private val restClient: RestClient,
    private val webhookUrl: String
) : NotificationAdapter {

    override fun send(request: NotificationRequest) {
        val body = mapOf(
            "type" to "message",
            "attachments" to listOf(
                mapOf(
                    "contentType" to "application/vnd.microsoft.card.adaptive",
                    "content" to mapOf(
                        "type" to "AdaptiveCard",
                        "version" to "1.2",
                        "body" to listOf(
                            mapOf(
                                "type" to "TextBlock",
                                "text" to request.subject,
                                "weight" to "bolder",
                                "size" to "medium"
                            ),
                            mapOf(
                                "type" to "TextBlock",
                                "text" to request.body,
                                "wrap" to true
                            )
                        )
                    )
                )
            )
        )

        restClient.post()
            .uri(webhookUrl)
            .body(body)
            .retrieve()
            .toBodilessEntity()
    }
}
