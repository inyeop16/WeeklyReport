package com.pluxity.weeklyreport.notification.dto

data class NotificationRequest(
    val subject: String,
    val body: String,
    val recipients: List<String>
)
