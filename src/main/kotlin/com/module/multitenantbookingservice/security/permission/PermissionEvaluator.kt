package com.module.multitenantbookingservice.security.permission

import com.module.multitenantbookingservice.core.models.UserProfile
import com.module.multitenantbookingservice.core.service.UserProfileService
import com.module.multitenantbookingservice.core.service.PermissionReloadEvent
import com.module.multitenantbookingservice.security.annotation.Permission
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

    override fun hasPermission(userId: UUID, permission: String): Boolean {
        return try {
            val userPermissions = userPermissionService.getUserPermissions(userId)
            matchesPermission(userPermissions, permission)
        } catch (e: Exception) {
            // Log exception and deny access by default
            false
        }
    }


    /**
     * 检查用户权限是否匹配所需权限
     * 支持通配符匹配 (如 "orders:*" 匹配 "orders:read")
     * 支持系统全权限 "*:*"
     */
    private fun matchesPermission(userPermissions: Set<String>, requiredPermission: String): Boolean {

        // 系统全权限
        if (userPermissions.contains(Permission.SYSTEM_ALL.toString())) {
            return true
        }

        // 直接匹配
        if (userPermissions.contains(requiredPermission)) {
            return true
        }


        // 通配符匹配 domain:*
        // 例如: "orders:*" 匹配 "orders:read"
        val parts = requiredPermission.split(":")
        if (parts.size != 2) {
            return false
        }

        val domain = parts.first()
        val wildcardActionPermission = "$domain:*"  // 例如 "orders:*"
        if (userPermissions.contains(wildcardActionPermission)) {
            return true
        }

        // 通配符 *:action

        val action = parts.last()
        val wildcardDomainPermission = "*:$action" // 例如 "*:read"
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