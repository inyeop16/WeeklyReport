package com.pluxity.weeklyreport.notification.config

import com.pluxity.weeklyreport.notification.EmailNotificationAdapter
import com.pluxity.weeklyreport.notification.NotificationAdapter
import com.pluxity.weeklyreport.notification.TeamsNotificationAdapter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(NotificationProperties::class)
class NotificationAdapterConfig(
    private val notificationProperties: NotificationProperties
) {

    @Bean
    fun notificationAdapter(mailSender: JavaMailSender): NotificationAdapter =
        when (notificationProperties.provider) {
            "email" -> EmailNotificationAdapter(mailSender, notificationProperties.email.from)
            "teams" -> TeamsNotificationAdapter(
                RestClient.builder()
                    .defaultHeader("content-type", "application/json")
                    .build(),
                notificationProperties.teams.webhookUrl
            )
            else -> throw IllegalArgumentException("Unknown notification provider: ${notificationProperties.provider}")
        }
}
