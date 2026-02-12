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
)

fun DailyEntry.toResponse() = DailyEntryResponse(
    id = id,
    userId = user.id,
    entryDate = entryDate,
    content = content,
    category = category,
    createdAt = createdAt
)