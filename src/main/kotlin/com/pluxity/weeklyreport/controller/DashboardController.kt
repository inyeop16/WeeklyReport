package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.response.DashboardResponse
import com.pluxity.weeklyreport.service.DashboardService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping
    fun getDashboard(
        @RequestParam departmentId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) weekStart: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) weekEnd: LocalDate
    ): ResponseEntity<DashboardResponse> =
        ResponseEntity.ok(dashboardService.getDashboard(departmentId, weekStart, weekEnd))
}
