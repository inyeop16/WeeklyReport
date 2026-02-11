package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.response.DepartmentResponse
import com.pluxity.weeklyreport.service.DepartmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/departments")
class DepartmentController(
    private val departmentService: DepartmentService
) {

    @GetMapping
    fun findAll(): ResponseEntity<List<DepartmentResponse>> =
        ResponseEntity.ok(departmentService.findAll())
}
