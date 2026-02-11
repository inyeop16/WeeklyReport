package com.pluxity.weeklyreport.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String,
    val expiration: Long
)
