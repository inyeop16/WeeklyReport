package com.pluxity.weeklyreport.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateTemplateRequest(
    @field:NotBlank(message = "템플릿 이름은 필수입니다")
    val name: String,

    @field:NotBlank(message = "시스템 프롬프트는 필수입니다")
    val systemPrompt: String,

    val department: String? = null
)
