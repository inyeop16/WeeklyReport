package com.pluxity.weeklyreport.dto.request

import java.time.LocalDate

data class SendReportRequest(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val reportId: Long
)
