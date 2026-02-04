package com.module.app.security.web.controller

import com.module.app.security.model.User
import com.module.app.security.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @GetMapping("/private/auth/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: User,
        request: HttpServletRequest
    ): ResponseEntity<UserDetails> {
        logger.debug("User profile requested - user: ${userDetails.id}, username: ${userDetails.username}")
        return ResponseEntity(userDetails, HttpStatus.OK)
    }

    @GetMapping("/private/auth/logout")
    fun logout(
        response: HttpServletResponse,
        @AuthenticationPrincipal userDetails: User?
    ): ResponseEntity<Map<String, String>> {
        val userId = userDetails?.id
        logger.info("User logout initiated - user: $userId")

        authService.logout(response)

        logger.info("User logout completed successfully - user: $userId")
        return ResponseEntity.ok(mapOf("message" to "OK"))
    }

    @GetMapping("/public/auth/login-endpoint")
    fun getLoginEndpoint(request: HttpServletRequest): ResponseEntity<Map<String, String>> {
        logger.debug("Login endpoint requested from IP: ${request.remoteAddr}")
        val endpoint = authService.getLoginEndpoint()
        logger.debug("Login endpoint provided: $endpoint")
        return ResponseEntity.ok(mapOf("loginEndpoint" to endpoint))
    }
}