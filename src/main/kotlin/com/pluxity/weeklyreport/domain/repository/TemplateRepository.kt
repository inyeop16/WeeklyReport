package com.pluxity.weeklyreport.domain.repository

import com.pluxity.weeklyreport.domain.entity.Template
import org.springframework.data.jpa.repository.JpaRepository

interface TemplateRepository : JpaRepository<Template, Long> {
    fun findByActiveTrue(): List<Template>
    fun findByDepartmentAndActiveTrue(department: String): List<Template>
    fun findByNameAndActiveTrue(name: String): Template?
}
