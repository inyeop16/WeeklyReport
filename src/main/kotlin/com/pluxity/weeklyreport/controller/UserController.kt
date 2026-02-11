package com.pluxity.weeklyreport.controller

import com.pluxity.weeklyreport.dto.response.UserResponse
import com.pluxity.weeklyreport.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun findAll(): ResponseEntity<List<UserResponse>> =
        ResponseEntity.ok(userService.findAll())

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<UserResponse> =
        ResponseEntity.ok(userService.findById(id))
}
