package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.request.LoginRequest
import com.pluxity.weeklyreport.dto.request.SignupRequest
import com.pluxity.weeklyreport.dto.response.TokenResponse
import com.pluxity.weeklyreport.dto.response.UserResponse
import com.pluxity.weeklyreport.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<UserResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(authService.login(request))

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Map<String, String>> =
        ResponseEntity.ok(mapOf("message" to "클라이언트에서 토큰을 삭제해주세요"))
}
