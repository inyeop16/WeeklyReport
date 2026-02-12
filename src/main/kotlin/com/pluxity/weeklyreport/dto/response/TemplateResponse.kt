package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.Template

data class TemplateResponse(
    val id: Long,
    val name: String,
    val systemPrompt: String,
    val department: String?,
    val active: Boolean
)

fun Template.toResponse() = TemplateResponse(
    id = id,
    name = name,
    systemPrompt = systemPrompt,
    department = department,
    active = active
)
