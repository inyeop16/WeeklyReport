package com.pluxity.weeklyreport.dto

data class AiTaskDto(
    val project: String? = null,
    val description: String = "",
    val status: String? = null,
    val progress: Int? = null,
    val date: String? = null
)

data class AiTaskListDto(
    val tasks: List<AiTaskDto> = emptyList()
)
