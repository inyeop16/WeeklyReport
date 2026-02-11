package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CreateDailyEntryRequest(
    @field:NotNull(message = "사용자 ID는 필수입니다")
    val userId: Long,

    @field:NotNull(message = "날짜는 필수입니다")
    val entryDate: LocalDate,

    @field:NotBlank(message = "내용은 필수입니다")
    val content: String,

    val category: String? = null
)
