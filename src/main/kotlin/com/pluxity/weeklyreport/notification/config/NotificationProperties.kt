package com.pluxity.weeklyreport.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.notification")
data class NotificationProperties(
    val provider: String = "email",
    val teams: TeamsProperties = TeamsProperties(),
    val email: EmailProperties = EmailProperties()
) {
    data class TeamsProperties(
        val webhookUrl: String = ""
    )

    data class EmailProperties(
        val from: String = "noreply@pluxity.com"
    )
}
