package com.pluxity.weeklyreport.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "reports")
class Report(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "week_start", nullable = false)
    var weekStart: LocalDate,

    @Column(name = "week_end", nullable = false)
    var weekEnd: LocalDate,

    @Column(columnDefinition = "TEXT")
    var rendered: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_entries", columnDefinition = "jsonb")
    var rawEntries: String? = null,

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "sent_to", columnDefinition = "TEXT[]")
    var sentTo: Array<String>? = null,

    @Column(name = "created_at", updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
