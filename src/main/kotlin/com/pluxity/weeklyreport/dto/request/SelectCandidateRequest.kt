package com.pluxity.weeklyreport.dto.request

import java.time.LocalDate

data class SelectCandidateRequest(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val acceptCandidate: Boolean
)
