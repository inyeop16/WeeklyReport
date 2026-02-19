package com.pluxity.weeklyreport.dto

data class AiTaskDto(
    val project: String? = null,
    val description: String = "",
    val status: String? = null,
    val progress: Int? = null,
)

data class AiTaskListDto(
    val task: List<AiTaskDto> = emptyList()
)
