package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.request.CreateDepartmentRequest
import com.pluxity.weeklyreport.dto.response.DepartmentResponse
import com.pluxity.weeklyreport.service.DepartmentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/departments")
class DepartmentController(
    private val departmentService: DepartmentService
) {

    @GetMapping
    fun findAll(): ResponseEntity<List<DepartmentResponse>> =
        ResponseEntity.ok(departmentService.findAll())

    @PostMapping
    fun create(@Valid @RequestBody request: CreateDepartmentRequest): ResponseEntity<DepartmentResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateDepartmentRequest
    ): ResponseEntity<DepartmentResponse> =
        ResponseEntity.ok(departmentService.update(id, request))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        departmentService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
