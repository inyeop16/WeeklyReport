package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.request.GenerateReportRequest
import com.pluxity.weeklyreport.dto.request.ModifyReportRequest
import com.pluxity.weeklyreport.dto.request.SendReportRequest
import com.pluxity.weeklyreport.dto.response.ReportResponse
import com.pluxity.weeklyreport.service.ReportService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/reports")
class ReportController(
    private val reportService: ReportService
) {

    @PostMapping("/generate")
    fun generate(
        @Valid @RequestBody request: GenerateReportRequest,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<ReportResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(reportService.getOrGenerate(request, userId))

    @PostMapping("/regenerate")
    fun regenerate(
        @Valid @RequestBody request: GenerateReportRequest,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<ReportResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(reportService.regenerate(request, userId))

    @PostMapping("/modify")
    fun modify(
        @Valid @RequestBody request: ModifyReportRequest,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<ReportResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(reportService.modify(request, userId))

    @GetMapping("/versions")
    fun getVersions(
        @RequestParam weekStart: LocalDate,
        @RequestParam weekEnd: LocalDate,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<List<ReportResponse>> =
        ResponseEntity.ok(reportService.getVersions(userId, weekStart, weekEnd))

    @PostMapping("/send")
    fun send(
        @Valid @RequestBody request: SendReportRequest,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<ReportResponse> =
        ResponseEntity.ok(reportService.send(request, userId))

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<ReportResponse> =
        ResponseEntity.ok(reportService.findById(id))

    @GetMapping
    fun findByUserId(@AuthenticationPrincipal userId: Long): ResponseEntity<List<ReportResponse>> =
        ResponseEntity.ok(reportService.findByUserId(userId))
}
