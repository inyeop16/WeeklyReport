package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.DailyEntry
import java.time.LocalDate
import java.time.OffsetDateTime

data class DailyEntryResponse(
    val id: Long,
    val userId: Long,
    val entryDate: LocalDate,
    val content: String,
    val category: String?,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(entry: DailyEntry) = DailyEntryResponse(
            id = entry.id,
            userId = entry.user.id,
            entryDate = entry.entryDate,
            content = entry.content,
            category = entry.category,
            createdAt = entry.createdAt
        )
    }
}
