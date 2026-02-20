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

    @Column(name = "candidate_rendered", columnDefinition = "TEXT")
    var candidateRendered: String? = null,

    @Column(name = "candidate_tasks_json", columnDefinition = "TEXT")
    var candidateTasksJson: String? = null,

    @Column(name = "is_last", nullable = false)
    var isLast: Boolean = true,

    @Column(name = "is_sent", nullable = false)
    var isSent: Boolean = false,

    @Column(name = "created_at", updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) {
    @OneToMany(mappedBy = "report", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tasks: MutableList<ReportTask> = mutableListOf()
}
