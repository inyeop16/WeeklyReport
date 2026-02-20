package com.pluxity.weeklyreport.domain.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "report_tasks")
class ReportTask(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    var report: Report,

    @Column
    var project: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column
    var status: TaskStatus? = null,

    @Column
    var progress: Int? = null,

    @Column(name = "start_date")
    var startDate: LocalDate? = null,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
