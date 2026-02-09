package com.module.app.security.provider

import com.module.app.core.models.UserProfile
import com.module.app.core.service.UserProfileService
import io.github.common.permission.provider.PermissionRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component
import java.util.*

/**
 * Application-specific implementation of PermissionRepository.
 * Adapts the existing UserProfile/TenantRole permission model to the generic permission system.
 */
@Component
class ApplicationPermissionRepository(
    private val userProfileService: UserProfileService
) : PermissionRepository {

    override fun getUserPermissions(userId: UUID): Set<String> {
        val userProfile = userProfileService.getUserProfileInternal(userId)
        return collectAllPermissions(userProfile)
    }

    @CacheEvict(value = ["userPermissions"], key = "#userId")
    override fun evictUserPermissions(userId: UUID) {
        // Cache eviction handled by annotation
    }

    /**
     * Collect all permissions from the user's tenant roles.
     * This adapts the existing permission collection logic to work with the starter.
     */
    private fun collectAllPermissions(userProfile: UserProfile): Set<String> {
        val permissions = mutableSetOf<String>()

        userProfile.tenantRoles.forEach { role ->
            permissions.addAll(role.permissions)
        }

        return permissions
    }
}