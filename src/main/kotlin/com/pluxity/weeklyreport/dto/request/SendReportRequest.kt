package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class SendReportRequest(
    @field:NotNull(message = "보고서 ID는 필수입니다")
    val reportId: Long,

    @field:NotEmpty(message = "수신자 목록은 필수입니다")
    val recipients: List<String>
)
