package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.domain.entity.Department
import com.pluxity.weeklyreport.domain.repository.DepartmentRepository
import com.pluxity.weeklyreport.dto.request.CreateDepartmentRequest
import com.pluxity.weeklyreport.dto.response.DepartmentResponse
import com.pluxity.weeklyreport.exception.BusinessException
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

    @Transactional
    fun create(request: CreateDepartmentRequest): DepartmentResponse {
        if (departmentRepository.existsByName(request.name)) {
            throw BusinessException("이미 존재하는 부서명입니다")
        }
        val department = Department(name = request.name)
        return DepartmentResponse.from(departmentRepository.save(department))
    }

    @Transactional
    fun update(id: Long, request: CreateDepartmentRequest): DepartmentResponse {
        val department = departmentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Department", "id", id) }

        if (department.name != request.name && departmentRepository.existsByName(request.name)) {
            throw BusinessException("이미 존재하는 부서명입니다")
        }

        department.name = request.name
        return DepartmentResponse.from(departmentRepository.save(department))
    }

    @Transactional
    fun delete(id: Long) {
        if (!departmentRepository.existsById(id)) {
            throw ResourceNotFoundException("Department", "id", id)
        }
        departmentRepository.deleteById(id)
    }

    fun getEntity(id: Long) =
        departmentRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Department", "id", id) }
}
