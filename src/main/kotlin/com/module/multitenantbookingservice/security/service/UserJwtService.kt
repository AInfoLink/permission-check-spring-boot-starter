package com.module.multitenantbookingservice.security.service

import com.app.security.repository.model.User
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.stereotype.Service
import java.util.*


@Service
class UserJwtService(
    val webProperties: WebProperties,
    val defaultJwtService: DefaultJwtService,
) : JwtService<User> {
    override lateinit var secret: ByteArray
    val IDENTITY_KEY = "jwt"

    @PostConstruct
    fun postConstruct() {
        secret = Base64.getDecoder().decode(webProperties.jwtSecret)
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
            isVerified = claims["isVerified"] as Boolean
        )
    }

    override fun isValidToken(token: String): Boolean {
        return defaultJwtService.isValidToken(token)
    }
}