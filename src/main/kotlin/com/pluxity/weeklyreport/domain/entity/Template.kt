package com.pluxity.weeklyreport.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "templates")
class Template(

    @Column(nullable = false)
    var name: String,

    @Column(name = "system_prompt", columnDefinition = "TEXT", nullable = false)
    var systemPrompt: String,

    @Column
    var department: String? = null,

    @Column(nullable = false)
    var active: Boolean = true,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
