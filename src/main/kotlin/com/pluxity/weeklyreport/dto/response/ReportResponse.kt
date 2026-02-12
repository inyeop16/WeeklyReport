package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.Report
import java.time.LocalDate
import java.time.OffsetDateTime

data class ReportResponse(
    val id: Long,
    val userId: Long,
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val rendered: String?,
    val rawEntries: String?,
    val sentTo: List<String>?,
    val createdAt: OffsetDateTime
)

fun Report.toResponse() = ReportResponse(
    id = id,
    userId = user.id,
    weekStart = weekStart,
    weekEnd = weekEnd,
    rendered = rendered,
    rawEntries = rawEntries,
    sentTo = sentTo?.toList(),
    createdAt = createdAt
)
