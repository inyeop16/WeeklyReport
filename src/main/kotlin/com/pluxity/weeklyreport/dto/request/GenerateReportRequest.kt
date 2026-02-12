package com.pluxity.weeklyreport.dto.request

import java.time.LocalDate

data class GenerateReportRequest(
    val weekStart: LocalDate,

    val weekEnd: LocalDate
)
