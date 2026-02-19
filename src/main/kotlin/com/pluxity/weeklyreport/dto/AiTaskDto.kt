package com.pluxity.weeklyreport.dto

import java.time.LocalDate

data class AiTaskDto(
    val project: String? = null,
    val description: String = "",
    val status: String? = null,
    val progress: Int? = null,
    val date: LocalDate? = null
)

data class AiTaskListDto(
    val task: List<AiTaskDto> = emptyList()
)
