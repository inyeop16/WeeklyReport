package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class GenerateReportRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다")
    val userId: Long,

    @field:NotBlank(message = "주 시작일은 필수입니다")
    val weekStart: LocalDate,

    @field:NotBlank(message = "주 종료일은 필수입니다")
    val weekEnd: LocalDate
)
