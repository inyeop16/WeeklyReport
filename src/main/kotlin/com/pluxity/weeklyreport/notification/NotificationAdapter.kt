package com.pluxity.weeklyreport.notification

import com.pluxity.weeklyreport.notification.dto.NotificationRequest

interface NotificationAdapter {
    fun send(request: NotificationRequest)
}
