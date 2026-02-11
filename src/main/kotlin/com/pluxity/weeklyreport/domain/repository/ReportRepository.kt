package com.pluxity.weeklyreport.domain.repository

import com.pluxity.weeklyreport.domain.entity.Report
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long> {
    fun findByUserId(userId: Long): List<Report>
}
