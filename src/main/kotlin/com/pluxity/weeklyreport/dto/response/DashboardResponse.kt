package com.pluxity.weeklyreport.dto.response

import java.time.LocalDate

data class DashboardResponse(
    val department: String,
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val teams: List<TeamTasksResponse>
)

data class TeamTasksResponse(
    val username: String,
    val tasks: List<ReportTaskResponse>
)
