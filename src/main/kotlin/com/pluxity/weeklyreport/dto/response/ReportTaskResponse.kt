package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.ReportTask
import java.time.LocalDate

data class ReportTaskResponse(
    val id: Long,
    val project: String?,
    val description: String,
    val status: String?,
    val progress: Int?,
    val startDate: LocalDate?,
    val endDate: LocalDate?
)

fun ReportTask.toResponse() = ReportTaskResponse(
    id = id,
    project = project,
    description = description,
    status = status?.name,
    progress = progress,
    startDate = startDate,
    endDate = endDate
)
