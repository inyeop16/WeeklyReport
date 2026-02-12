package com.pluxity.weeklyreport.dto.response

import com.pluxity.weeklyreport.domain.entity.User

data class UserResponse(
    val id: Long,
    val username: String,
    val name: String,
    val email: String,
    val department: DepartmentResponse?,
    val role: String
)

fun User.toResponse() = UserResponse(
    id = id,
    username = username,
    name = name,
    email = email,
    department = department?.let { DepartmentResponse.from(it) },
    role = role.name
)
