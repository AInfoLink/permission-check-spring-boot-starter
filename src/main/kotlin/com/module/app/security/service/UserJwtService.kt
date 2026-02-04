package com.module.app.security.service

import com.module.app.security.model.User
import com.module.app.security.config.SecurityProperties
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import java.util.*


@Service
class UserJwtService(
    val securityProperties: SecurityProperties,
    val defaultJwtService: DefaultJwtService,
) : JwtService<User> {
    override lateinit var secret: ByteArray
    val IDENTITY_KEY = "jwt"

    @PostConstruct
    fun postConstruct() {
        secret = Base64.getDecoder().decode(securityProperties.jwtSecret)
    }

    override fun parseToken(token: String): User? {
        val claims = defaultJwtService.parseToken(token) ?: return null
        return User(
            id = UUID.fromString(claims["sub"] as String),
            email = claims["email"] as String,
            username = claims["username"] as String,
            systemRoles = (claims["systemRoles"] as List<*>).map { it.toString() }.toMutableSet(),
            password = "(sensitive)",
            provider = claims["provider"] as String,
            isEmailVerified = claims["isVerified"] as Boolean
        )
    }

    override fun isValidToken(token: String): Boolean {
        return defaultJwtService.isValidToken(token)
    }
}