package com.pluxity.weeklyreport.dto.response

import java.time.OffsetDateTime

data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)
