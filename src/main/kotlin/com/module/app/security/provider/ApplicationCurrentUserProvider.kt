package com.module.app.security.provider

import com.module.app.security.model.User
import com.module.app.utils.Safe
import io.github.common.permission.provider.CurrentUserProvider
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

/**
 * Application-specific implementation of CurrentUserProvider.
 * Adapts the existing Spring Security User model to the generic permission system.
 */
@Component
class ApplicationCurrentUserProvider : CurrentUserProvider {

    private val logger = LoggerFactory.getLogger(ApplicationCurrentUserProvider::class.java)

    override fun getCurrentUserId(): UUID? {
        return Safe.call(logger) {
            logger.debug("Retrieving current user ID from security context")
            val context = SecurityContextHolder.getContext()
            val authentication = context.authentication

            if (authentication == null) {
                logger.warn("No authentication found in security context")
                return@call null
            }

            val principal = authentication.principal
            val user = principal as? User

            if (user == null) {
                logger.warn("Principal is not a User instance: ${principal?.javaClass?.simpleName}")
                return@call null
            }

            user.id
        }
    }

    override fun getCurrentUserContext(): Map<String, Any> {
        return Safe.call(logger) {
            val context = SecurityContextHolder.getContext()
            val authentication = context.authentication
            val user = authentication?.principal as? User

            if (user != null) {
                mapOf(
                    "email" to user.email,
                    "provider" to user.provider,
                    "systemRoles" to user.systemRoles,
                    "isEmailVerified" to user.isEmailVerified,
                    "isPhoneVerified" to user.isPhoneVerified
                )
            } else {
                emptyMap()
            }
        } ?: emptyMap()
    }
}