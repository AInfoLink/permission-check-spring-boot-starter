package io.github.common.permission.service

import io.github.common.permission.provider.PermissionRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import java.util.*

/**
 * Cached implementation of PermissionService that delegates to a PermissionRepository.
 * Provides automatic caching with Spring Cache abstraction.
 */
class CachedPermissionService(
    private val permissionRepository: PermissionRepository
) : PermissionService {

    @Cacheable(value = ["userPermissions"], key = "#userId")
    override fun getUserPermissions(userId: UUID): Set<String> {
        return permissionRepository.getUserPermissions(userId)
    }

    @CacheEvict(value = ["userPermissions"], key = "#userId")
    override fun evictUserPermissions(userId: UUID) {
        // Cache eviction handled by annotation, but also delegate to repository
        permissionRepository.evictUserPermissions(userId)
    }
}