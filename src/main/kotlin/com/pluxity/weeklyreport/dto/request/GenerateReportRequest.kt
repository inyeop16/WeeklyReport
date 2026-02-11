package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class GenerateReportRequest(
    @field:NotNull(message = "사용자 ID는 필수입니다")
    val userId: Long,

    @field:NotNull(message = "템플릿 ID는 필수입니다")
    val templateId: Long,

    @field:NotNull(message = "주 시작일은 필수입니다")
    val weekStart: LocalDate,

    @field:NotNull(message = "주 종료일은 필수입니다")
    val weekEnd: LocalDate
)
