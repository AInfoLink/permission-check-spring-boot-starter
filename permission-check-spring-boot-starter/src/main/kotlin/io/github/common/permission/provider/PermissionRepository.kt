package io.github.common.permission.provider

import java.util.*

/**
 * Interface for permission data access - completely domain agnostic.
 * Implementations should adapt their specific user/role/permission models
 * to this simple contract.
 */
interface PermissionRepository {
    /**
     * Get all permissions for a user.
     * The permission strings should follow a consistent format but the format
     * is entirely up to the implementing application.
     *
     * Common patterns include:
     * - "resource:action" (e.g., "orders:read", "users:create")
     * - "domain:action" (e.g., "billing:view", "admin:manage")
     * - Hierarchical "domain:subdomain:action" (e.g., "system:users:create")
     * - Wildcard support "*:read", "admin:*", "*:*"
     *
     * @param userId User identifier
     * @return Set of permission strings (format determined by application)
     */
    fun getUserPermissions(userId: UUID): Set<String>

    /**
     * Optional: Evict permission cache for a user.
     * This is called when permission data might have changed and caches
     * need to be refreshed. Implementations can choose to ignore this
     * if they don't use caching or handle cache invalidation differently.
     *
     * @param userId User identifier
     */
    fun evictUserPermissions(userId: UUID) {
        // Default implementation does nothing - caching is optional
    }
}