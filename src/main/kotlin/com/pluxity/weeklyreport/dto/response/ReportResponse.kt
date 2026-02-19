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
    val candidateRendered: String?,
    val rawEntries: String?,
    val isLast: Boolean,
    val isSent: Boolean,
    val createdAt: OffsetDateTime,
    val tasks: List<ReportTaskResponse> = emptyList()
)

fun Report.toResponse() = ReportResponse(
    id = id,
    userId = user.id,
    weekStart = weekStart,
    weekEnd = weekEnd,
    rendered = rendered,
    candidateRendered = candidateRendered,
    rawEntries = rawEntries,
    isLast = isLast,
    isSent = isSent,
    createdAt = createdAt,
    tasks = tasks.map { it.toResponse() }
)
