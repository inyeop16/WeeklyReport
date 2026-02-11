package com.pluxity.weeklyreport.service

import com.pluxity.weeklyreport.auth.JwtProperties
import com.pluxity.weeklyreport.auth.JwtTokenProvider
import com.pluxity.weeklyreport.domain.entity.User
import com.pluxity.weeklyreport.domain.repository.UserRepository
import com.pluxity.weeklyreport.dto.request.LoginRequest
import com.pluxity.weeklyreport.dto.request.SignupRequest
import com.pluxity.weeklyreport.dto.response.TokenResponse
import com.pluxity.weeklyreport.dto.response.UserResponse
import com.pluxity.weeklyreport.exception.BusinessException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val departmentService: DepartmentService
) {

    @Transactional
    fun signup(request: SignupRequest): UserResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw BusinessException("이미 존재하는 사용자명입니다")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw BusinessException("이미 존재하는 이메일입니다")
        }

        val department = request.departmentId?.let { departmentService.getEntity(it) }

        val user = User(
            username = request.username,
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            department = department
        )

        return UserResponse.from(userRepository.save(user))
    }

    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw BusinessException("사용자명 또는 비밀번호가 올바르지 않습니다")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BusinessException("사용자명 또는 비밀번호가 올바르지 않습니다")
        }

        val token = jwtTokenProvider.generateToken(user.id, user.username, user.role.name)
        val expiresIn = jwtProperties.expiration / 1000

        return TokenResponse(
            accessToken = token,
            expiresIn = expiresIn
        )
    }
}
