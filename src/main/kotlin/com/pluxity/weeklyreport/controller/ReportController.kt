package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.request.GenerateReportRequest
import com.pluxity.weeklyreport.dto.request.SendReportRequest
import com.pluxity.weeklyreport.dto.request.UpdateReportRequest
import com.pluxity.weeklyreport.dto.response.ReportResponse
import com.pluxity.weeklyreport.service.ReportService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reports")
class ReportController(
    private val reportService: ReportService
) {

    @PostMapping("/generate")
    fun generate(@Valid @RequestBody request: GenerateReportRequest): ResponseEntity<ReportResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(reportService.generate(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateReportRequest
    ): ResponseEntity<ReportResponse> =
        ResponseEntity.ok(reportService.update(id, request))

    @PostMapping("/send")
    fun send(@Valid @RequestBody request: SendReportRequest): ResponseEntity<ReportResponse> =
        ResponseEntity.ok(reportService.send(request))

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<ReportResponse> =
        ResponseEntity.ok(reportService.findById(id))

    @GetMapping
    fun findByUserId(@RequestParam userId: Long): ResponseEntity<List<ReportResponse>> =
        ResponseEntity.ok(reportService.findByUserId(userId))
}
