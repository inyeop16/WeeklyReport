package com.pluxity.weeklyreport.domain.repository

import com.pluxity.weeklyreport.domain.entity.ReportTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ReportTaskRepository : JpaRepository<ReportTask, Long> {

    fun findByReportId(reportId: Long): List<ReportTask>

    @Query("""
        SELECT t FROM ReportTask t
        JOIN FETCH t.report r
        JOIN FETCH r.user u
        WHERE u.department.id = :departmentId
          AND r.weekStart = :weekStart
          AND r.weekEnd = :weekEnd
          AND r.isLast = true
        ORDER BY u.name, t.id
    """)
    fun findByDepartmentAndWeek(
        departmentId: Long,
        weekStart: LocalDate,
        weekEnd: LocalDate
    ): List<ReportTask>
}
