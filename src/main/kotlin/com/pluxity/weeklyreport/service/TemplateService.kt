package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.domain.entity.Template
import com.pluxity.weeklyreport.domain.repository.TemplateRepository
import com.pluxity.weeklyreport.dto.request.CreateTemplateRequest
import com.pluxity.weeklyreport.dto.request.UpdateTemplateRequest
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

    fun findAll(): List<TemplateResponse> =
        templateRepository.findAll().map { it.toResponse() }

    @Transactional
    fun update(id: Long, request: UpdateTemplateRequest): TemplateResponse {
        val template = templateRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Template", "id", id) }

        request.name?.let { template.name = it }
        request.systemPrompt?.let { template.systemPrompt = it }
        request.department?.let { template.department = it }
        request.active?.let { template.active = it }

        return templateRepository.save(template).toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        if (!templateRepository.existsById(id)) {
            throw ResourceNotFoundException("Template", "id", id)
        }
        templateRepository.deleteById(id)
    }

    fun getEntity(id: Long): Template =
        templateRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Template", "id", id) }
}
