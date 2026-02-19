package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.domain.repository.ReportTaskRepository
import com.pluxity.weeklyreport.dto.response.DashboardResponse
import com.pluxity.weeklyreport.dto.response.TeamTasksResponse
import com.pluxity.weeklyreport.dto.response.toResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class DashboardService(
    private val reportTaskRepository: ReportTaskRepository,
    private val departmentService: DepartmentService
) {

    fun getDashboard(departmentId: Long, weekStart: LocalDate, weekEnd: LocalDate): DashboardResponse {
        val department = departmentService.getEntity(departmentId)

        val tasks = reportTaskRepository.findByDepartmentAndWeek(departmentId, weekStart, weekEnd)

        val teams = tasks.groupBy { it.report.user }
            .map { (user, userTasks) ->
                TeamTasksResponse(
                    username = user.name,
                    tasks = userTasks.map { it.toResponse() }
                )
            }

        return DashboardResponse(
            department = department.name,
            weekStart = weekStart,
            weekEnd = weekEnd,
            teams = teams
        )
    }
}
