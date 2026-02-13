package com.pluxity.weeklyreport.domain.repository

import com.pluxity.weeklyreport.domain.entity.Report
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ReportRepository : JpaRepository<Report, Long> {

    fun findByUserIdAndIsLastTrue(userId: Long): List<Report>

    fun findByUserIdAndWeekStartAndWeekEndAndIsLastTrue(
        userId: Long, weekStart: LocalDate, weekEnd: LocalDate
    ): Report?

    fun findByUserIdAndWeekStartAndWeekEndOrderByCreatedAtDesc(
        userId: Long, weekStart: LocalDate, weekEnd: LocalDate
    ): List<Report>
}
