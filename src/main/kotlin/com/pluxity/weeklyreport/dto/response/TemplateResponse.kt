package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.Template

data class TemplateResponse(
    val id: Long,
    val name: String,
    val systemPrompt: String,
    val department: String?,
    val active: Boolean
) {
    companion object {
        fun from(template: Template) = TemplateResponse(
            id = template.id,
            name = template.name,
            systemPrompt = template.systemPrompt,
            department = template.department,
            active = template.active
        )
    }
}
