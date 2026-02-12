package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class CreateDailyEntryRequest(
    val entryDate: LocalDate,

    @field:NotBlank(message = "내용은 필수입니다")
    val content: String,

    val category: String? = null
)
