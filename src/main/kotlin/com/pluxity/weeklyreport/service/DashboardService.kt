package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.domain.repository.ReportTaskRepository
import com.pluxity.weeklyreport.dto.response.DashboardResponse
import com.pluxity.weeklyreport.dto.response.TeamTasksResponse
import com.pluxity.weeklyreport.dto.response.toResponse
import com.pluxity.weeklyreport.exception.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class DashboardService(
    private val reportTaskRepository: ReportTaskRepository,
    private val userService: UserService
) {

    fun getDashboard(userId: Long, weekStart: LocalDate, weekEnd: LocalDate): DashboardResponse {
        val user = userService.getEntity(userId)
        val department = user.department
            ?: throw BusinessException("소속 부서가 없습니다")

        val tasks = reportTaskRepository.findByDepartmentAndWeek(department.id, weekStart, weekEnd)

        val teams = tasks.groupBy { it.report.user }
            .map { (member, userTasks) ->
                TeamTasksResponse(
                    username = member.name,
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
