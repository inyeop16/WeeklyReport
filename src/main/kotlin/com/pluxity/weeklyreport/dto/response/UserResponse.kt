package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.User

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val department: String?
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            department = user.department
        )
    }
}
