package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateDepartmentRequest(
    @field:NotBlank(message = "부서명은 필수입니다")
    val name: String
)
