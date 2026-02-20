package com.pluxity.weeklyreport.domain.repository

import com.pluxity.weeklyreport.domain.entity.DailyEntry
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface DailyEntryRepository : JpaRepository<DailyEntry, Long> {
    fun findByUserId(userId: Long): List<DailyEntry>
    fun findByUserIdAndEntryDateBetweenOrderByCreatedAtDesc(userId: Long, start: LocalDate, end: LocalDate): List<DailyEntry>
}
