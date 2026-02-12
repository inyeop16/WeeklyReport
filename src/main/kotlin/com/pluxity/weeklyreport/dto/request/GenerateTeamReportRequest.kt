package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class GenerateTeamReportRequest(
    @field:NotNull(message = "부서 ID는 필수입니다")
    val departmentId: Long,

    val templateId: Long? = null,

    val weekStart: LocalDate,

    val weekEnd: LocalDate
)
