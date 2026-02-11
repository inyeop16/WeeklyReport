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
) {
    companion object {
        fun from(report: Report) = ReportResponse(
            id = report.id,
            userId = report.user.id,
            weekStart = report.weekStart,
            weekEnd = report.weekEnd,
            rendered = report.rendered,
            rawEntries = report.rawEntries,
            sentTo = report.sentTo?.toList(),
            createdAt = report.createdAt
        )
    }
}
