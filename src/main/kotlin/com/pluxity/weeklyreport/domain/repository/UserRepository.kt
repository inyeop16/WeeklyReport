package com.pluxity.weeklyreport.domain.repository

import com.pluxity.weeklyreport.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun findByEmail(email: String): User?
    fun findByUsername(username: String): User?
}
