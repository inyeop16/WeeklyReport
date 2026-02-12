package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.domain.entity.Template
import com.pluxity.weeklyreport.domain.repository.TemplateRepository
import com.pluxity.weeklyreport.dto.request.CreateTemplateRequest
import com.pluxity.weeklyreport.dto.response.TemplateResponse
import com.pluxity.weeklyreport.dto.response.toResponse
import com.pluxity.weeklyreport.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TemplateService(
    private val templateRepository: TemplateRepository
) {

    @Transactional
    fun create(request: CreateTemplateRequest): TemplateResponse {
        val template = Template(
            name = request.name,
            systemPrompt = request.systemPrompt,
            department = request.department
        )
        return templateRepository.save(template).toResponse()
    }

    fun findActive(department: String?): List<TemplateResponse> {
        val templates = if (department != null) {
            templateRepository.findByDepartmentAndActiveTrue(department)
        } else {
            templateRepository.findByActiveTrue()
        }
        return templates.map{ it.toResponse() }
    }

    fun findById(id: Long): TemplateResponse {
        val template = templateRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Template", "id", id) }
        return template.toResponse()
    }

    fun getEntity(id: Long): Template =
        templateRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Template", "id", id) }
}
