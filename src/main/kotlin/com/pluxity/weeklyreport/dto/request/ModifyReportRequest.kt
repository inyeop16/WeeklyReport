package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class ModifyReportRequest(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    @field:NotBlank(message = "수정 지시사항은 필수입니다")
    val instruction: String
)
