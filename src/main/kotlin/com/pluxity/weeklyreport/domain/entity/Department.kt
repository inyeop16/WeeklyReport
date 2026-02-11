package com.pluxity.weeklyreport.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "departments")
class Department(

    @Column(nullable = false, unique = true)
    var name: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
