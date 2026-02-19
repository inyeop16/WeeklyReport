package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.domain.entity.DailyEntry
import com.pluxity.weeklyreport.domain.repository.DailyEntryRepository
import com.pluxity.weeklyreport.dto.request.CreateDailyEntryRequest
import com.pluxity.weeklyreport.dto.response.DailyEntryResponse
import com.pluxity.weeklyreport.dto.response.toResponse
import com.pluxity.weeklyreport.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DailyEntryService(
    private val dailyEntryRepository: DailyEntryRepository,
    private val userService: UserService
) {

    @Transactional
    fun create(request: CreateDailyEntryRequest, userId: Long): DailyEntryResponse {
        val user = userService.getEntity(userId)

        val entry = DailyEntry(
            user = user,
            entryDate = request.entryDate,
            content = request.content,
        )
        return dailyEntryRepository.save(entry).toResponse()
    }

    fun findByUserId(userId: Long): List<DailyEntryResponse> =
        dailyEntryRepository.findByUserId(userId).map { it.toResponse() }

    fun findById(id: Long): DailyEntryResponse {
        val entry = dailyEntryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("DailyEntry", "id", id) }
        return entry.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        if (!dailyEntryRepository.existsById(id)) {
            throw ResourceNotFoundException("DailyEntry", "id", id)
        }
        dailyEntryRepository.deleteById(id)
    }
}
