package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.Department

data class DepartmentResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(department: Department) = DepartmentResponse(
            id = department.id,
            name = department.name
        )
    }
}
