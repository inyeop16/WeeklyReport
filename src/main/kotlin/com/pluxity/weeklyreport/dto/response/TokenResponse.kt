package com.pluxity.weeklyreport.dto.response

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val userId: Long
)
