package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotBlank

data class UpdateReportRequest(
    @field:NotBlank(message = "수정 지시사항은 필수입니다")
    val instruction: String
)
