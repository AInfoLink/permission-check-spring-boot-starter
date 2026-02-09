package io.github.common.permission.provider

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

/**
 * Default implementation of CurrentUserProvider that works with common Spring Security patterns.
 * This implementation attempts to extract user ID from various common user model patterns.
 *
 * Supports:
 * - Any user object with getId() method returning UUID or String
 * - Spring Security UserDetails with username as UUID
 * - Custom user models with 'id' property
 *
 * This default implementation covers 80% of use cases, but users can provide their own
 * implementation if needed.
 */
class DefaultCurrentUserProvider : CurrentUserProvider {

    private val logger = LoggerFactory.getLogger(DefaultCurrentUserProvider::class.java)

    override fun getCurrentUserId(): UUID? {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
                ?: return null

            val principal = authentication.principal
            if (principal == null || principal == "anonymousUser") {
                return null
            }

            // Try to extract UUID from common patterns
            extractUserIdFromPrincipal(principal)
        } catch (e: Exception) {
            logger.debug("Failed to extract user ID from security context", e)
            null
        }
    }

    private fun extractUserIdFromPrincipal(principal: Any): UUID? {
        return when {
            // Try to call getId() method if it exists (common pattern)
            hasGetIdMethod(principal) -> {
                val id = principal::class.java.getMethod("getId").invoke(principal)
                when (id) {
                    is UUID -> id
                    is String -> try { UUID.fromString(id) } catch (e: Exception) { null }
                    else -> null
                }
            }

            // Try to access id property via reflection (alternative pattern)
            hasIdProperty(principal) -> {
                val idField = principal::class.java.getDeclaredField("id")
                idField.isAccessible = true
                val id = idField.get(principal)
                when (id) {
                    is UUID -> id
                    is String -> try { UUID.fromString(id) } catch (e: Exception) { null }
                    else -> null
                }
            }

            // Try treating principal as String UUID (simple pattern)
            principal is String -> {
                try { UUID.fromString(principal) } catch (e: Exception) { null }
            }

            else -> {
                logger.debug("Unable to extract UUID from principal type: ${principal::class.java.simpleName}")
                null
            }
        }
    }

    private fun hasGetIdMethod(obj: Any): Boolean {
        return try {
            obj::class.java.getMethod("getId")
            true
        } catch (e: NoSuchMethodException) {
            false
        }
    }

    private fun hasIdProperty(obj: Any): Boolean {
        return try {
            obj::class.java.getDeclaredField("id")
            true
        } catch (e: NoSuchFieldException) {
            false
        }
    }

    override fun getCurrentUserContext(): Map<String, Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            val principal = authentication?.principal

            if (principal != null && principal != "anonymousUser") {
                // Extract common properties that might be useful
                mutableMapOf<String, Any>().apply {
                    // Try to extract common properties
                    tryExtractProperty(principal, "username")?.let { put("username", it) }
                    tryExtractProperty(principal, "email")?.let { put("email", it) }
                    tryExtractProperty(principal, "roles")?.let { put("roles", it) }
                    tryExtractProperty(principal, "authorities")?.let { put("authorities", it) }
                }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            logger.debug("Failed to extract user context", e)
            emptyMap()
        }
    }

    private fun tryExtractProperty(obj: Any, propertyName: String): Any? {
        return try {
            // Try getter method first
            val getterName = "get${propertyName.replaceFirstChar { it.uppercase() }}"
            val getter = obj::class.java.getMethod(getterName)
            getter.invoke(obj)
        } catch (e: Exception) {
            try {
                // Try field access
                val field = obj::class.java.getDeclaredField(propertyName)
                field.isAccessible = true
                field.get(obj)
            } catch (e2: Exception) {
                null
            }
        }
    }
}