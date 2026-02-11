package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.User

data class UserResponse(
    val id: Long,
    val username: String,
    val name: String,
    val email: String,
    val department: DepartmentResponse?,
    val role: String
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            username = user.username,
            name = user.name,
            email = user.email,
            department = user.department?.let { DepartmentResponse.from(it) },
            role = user.role.name
        )
    }
}
