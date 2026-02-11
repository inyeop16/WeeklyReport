package com.pluxity.weeklyreport.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column
    var password: String? = null,

    @Column
    var department: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
