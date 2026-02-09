package io.github.common.permission.service

import java.util.*

/**
 * Interface for permission data retrieval.
 * This is the internal interface used by the permission evaluation system.
 */
interface PermissionService {
    /**
     * Get all permissions for a user.
     * @param userId User identifier
     * @return Set of permission strings
     */
    fun getUserPermissions(userId: UUID): Set<String>

    /**
     * Evict permission cache for a user.
     * @param userId User identifier
     */
    fun evictUserPermissions(userId: UUID)
}