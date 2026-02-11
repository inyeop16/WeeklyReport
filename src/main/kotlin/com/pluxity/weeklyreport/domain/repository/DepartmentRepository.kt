package com.pluxity.weeklyreport.domain.repository

import com.pluxity.weeklyreport.domain.entity.Department
import org.springframework.data.jpa.repository.JpaRepository

interface DepartmentRepository : JpaRepository<Department, Long> {
    fun existsByName(name: String): Boolean
}
