package com.pluxity.weeklyreport.notification

import com.pluxity.weeklyreport.notification.dto.NotificationRequest
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class EmailNotificationAdapter(
    private val mailSender: JavaMailSender,
    private val fromAddress: String
) : NotificationAdapter {

    override fun send(request: NotificationRequest) {
        val message = SimpleMailMessage().apply {
            from = fromAddress
            setTo(*request.recipients.toTypedArray())
            subject = request.subject
            text = request.body
        }
        mailSender.send(message)
    }
}
