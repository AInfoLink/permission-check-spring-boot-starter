package com.module.app.security.permission

import com.module.app.core.models.UserProfile
import com.module.app.core.service.UserProfileService
import com.module.app.core.service.PermissionReloadEvent
import com.module.app.security.annotation.Permission
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class InMemoryCachedUserPermissionService(
    private val userProfileService: UserProfileService
): UserPermissionService {

    @Cacheable(value = ["userPermissions"], key = "#userId")
    override fun getUserPermissions(userId: UUID): Set<String> {
        val userProfile = userProfileService.getUserProfileInternal(userId)
        return collectAllPermissions(userProfile)
    }
    @CacheEvict(value = ["userPermissions"], key = "#userId")
    override fun evictUserPermissions(userId: UUID) {
        // Cache eviction handled by annotation
    }
    private fun collectAllPermissions(userProfile: UserProfile): Set<String> {
        val permissions = mutableSetOf<String>()

        userProfile.tenantRoles.forEach { role ->
            permissions.addAll(role.permissions)
        }

        return permissions
    }
}

@Component
class DefaultPermissionEvaluator(
    private val userPermissionService: UserPermissionService
): PermissionEvaluator {

    private val logger = LoggerFactory.getLogger(DefaultPermissionEvaluator::class.java)

    override fun hasPermission(userId: UUID, permission: String): Boolean {
        logger.debug("Evaluating permission '$permission' for user: $userId")

        val userPermissions = userPermissionService.getUserPermissions(userId)
        val hasPermission = matchesPermission(userPermissions, permission)

        if (!hasPermission) {
            logger.debug("Permission '$permission' denied for user: $userId - Available permissions: $userPermissions")
        } else {
            logger.debug("Permission '$permission' granted for user: $userId")
        }

        return hasPermission
    }


    /**
     * Check if user permissions match required permissions
     * Supports wildcard matching (e.g., "orders:*" matches "orders:read")
     * Supports system-wide permissions "*:*"
     */
    private fun matchesPermission(userPermissions: Set<String>, requiredPermission: String): Boolean {

        // System-wide permissions
        if (userPermissions.contains(Permission.SYSTEM_ALL.toString())) {
            return true
        }

        // Direct match
        if (userPermissions.contains(requiredPermission)) {
            return true
        }


        // Wildcard matching domain:*
        // Example: "orders:*" matches "orders:read"
        val parts = requiredPermission.split(":")
        if (parts.size != 2) {
            return false
        }

        val domain = parts.first()
        val wildcardActionPermission = "$domain:*"  // Example: "orders:*"
        if (userPermissions.contains(wildcardActionPermission)) {
            return true
        }

        // Wildcard *:action

        val action = parts.last()
        val wildcardDomainPermission = "*:$action" // Example: "*:read"
        if (userPermissions.contains(wildcardDomainPermission)) {
            return true
        }

        return false
    }

    @EventListener
    fun handlePermissionReloadEvent(event: PermissionReloadEvent) {
        userPermissionService.evictUserPermissions(event.userId)
    }
}