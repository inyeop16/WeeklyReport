package com.pluxity.weeklyreport.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.admin")
data class AdminProperties(
    val username: String = "admin",
    val name: String = "관리자",
    val email: String = "admin@pluxity.com",
    val password: String = "admin1234"
)
