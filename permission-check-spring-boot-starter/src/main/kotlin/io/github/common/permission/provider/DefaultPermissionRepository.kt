package io.github.common.permission.provider

import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

/**
 * Default implementation of PermissionRepository that works with Spring Security.
 *
 * Priority order:
 * 1. If user principal implements PermissionAware interface, use getPermissions() directly
 * 2. Otherwise, convert Spring Security authorities to permission strings
 *
 * This provides maximum flexibility while remaining simple for basic use cases.
 */
class DefaultPermissionRepository : PermissionRepository {

    private val logger = LoggerFactory.getLogger(DefaultPermissionRepository::class.java)

    override fun getUserPermissions(userId: UUID): Set<String> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication == null || !authentication.isAuthenticated) {
                return emptySet()
            }

            val principal = authentication.principal

            if (principal is PermissionAware) {
                logger.debug("Using PermissionAware interface for user $userId")
                return principal.getPermissions()
            }
            return emptySet()
        } catch (e: Exception) {
            logger.warn("Failed to get permissions for user $userId", e)
            emptySet()
        }
    }
}