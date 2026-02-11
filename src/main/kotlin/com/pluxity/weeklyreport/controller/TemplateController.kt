package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.request.CreateTemplateRequest
import com.pluxity.weeklyreport.dto.response.TemplateResponse
import com.pluxity.weeklyreport.service.TemplateService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/templates")
class TemplateController(
    private val templateService: TemplateService
) {

    @PostMapping
    fun create(@Valid @RequestBody request: CreateTemplateRequest): ResponseEntity<TemplateResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(templateService.create(request))

    @GetMapping
    fun findActive(@RequestParam(required = false) department: String?): ResponseEntity<List<TemplateResponse>> =
        ResponseEntity.ok(templateService.findActive(department))

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<TemplateResponse> =
        ResponseEntity.ok(templateService.findById(id))
}
