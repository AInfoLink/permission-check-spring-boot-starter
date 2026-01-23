package com.module.multitenantbookingservice.security.web.controller

import com.app.security.repository.model.User
import com.module.multitenantbookingservice.security.service.AuthService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class AuthController(
    val authService: AuthService
) {
    @GetMapping("/private/auth/me")
    fun getCurrentUser(@AuthenticationPrincipal userDetails: User): ResponseEntity<UserDetails> {
        return ResponseEntity(userDetails, HttpStatus.OK)
    }

    @GetMapping("/private/auth/logout")
    fun logout(
        response: HttpServletResponse
    ): ResponseEntity<Map<String, String>> {
        authService.logout(response)
        return ResponseEntity.ok(mapOf("message" to "OK"))
    }

    @GetMapping("/public/auth/login-endpoint")
    fun getLoginEndpoint(): ResponseEntity<Map<String, String>> {
        val endpoint = authService.getLoginEndpoint()
        return ResponseEntity.ok(mapOf("loginEndpoint" to endpoint))
    }
}