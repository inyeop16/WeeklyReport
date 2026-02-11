package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.domain.repository.DepartmentRepository
import com.pluxity.weeklyreport.dto.response.DepartmentResponse
import com.pluxity.weeklyreport.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DepartmentService(
    private val departmentRepository: DepartmentRepository
) {

    fun findAll(): List<DepartmentResponse> =
        departmentRepository.findAll().map(DepartmentResponse::from)

    fun getEntity(id: Long) =
        departmentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Department", "id", id) }
}
