package io.github.common.permission.service

import java.util.*

/**
 * Interface for evaluating whether a user has specific permissions.
 */
interface PermissionEvaluator {
    /**
     * Check if a user has the required permission.
     * @param userId User identifier
     * @param permission Permission string to check
     * @return true if user has permission, false otherwise
     */
    fun hasPermission(userId: UUID, permission: String): Boolean
}