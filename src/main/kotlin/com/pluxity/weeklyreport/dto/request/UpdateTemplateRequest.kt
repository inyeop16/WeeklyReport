package com.pluxity.weeklyreport.dto.request

data class UpdateTemplateRequest(
    val name: String? = null,
    val systemPrompt: String? = null,
    val department: String? = null,
    val active: Boolean? = null
)
