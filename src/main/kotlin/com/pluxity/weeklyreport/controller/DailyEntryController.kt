package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.request.CreateDailyEntryRequest
import com.pluxity.weeklyreport.dto.response.DailyEntryResponse
import com.pluxity.weeklyreport.service.DailyEntryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/daily-entries")
class DailyEntryController(
    private val dailyEntryService: DailyEntryService
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateDailyEntryRequest,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<DailyEntryResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(dailyEntryService.create(request, userId))

    @GetMapping
    fun findByUserId(@RequestParam userId: Long): ResponseEntity<List<DailyEntryResponse>> =
        ResponseEntity.ok(dailyEntryService.findByUserId(userId))

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<DailyEntryResponse> =
        ResponseEntity.ok(dailyEntryService.findById(id))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        dailyEntryService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
