package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.domain.entity.User
import com.pluxity.weeklyreport.domain.repository.UserRepository
import com.pluxity.weeklyreport.dto.response.UserResponse
import com.pluxity.weeklyreport.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    fun findAll(): List<UserResponse> =
        userRepository.findAll().map(UserResponse::from)

    fun findById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User", "id", id) }
        return UserResponse.from(user)
    }

    fun getEntity(id: Long): User =
        userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User", "id", id) }
}
