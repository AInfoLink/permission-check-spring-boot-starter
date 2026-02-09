package io.github.common.permission.service

import org.slf4j.LoggerFactory
import java.util.*

/**
 * Default implementation of PermissionEvaluator with support for wildcard matching.
 *
 * Supported permission patterns:
 * - Direct match: "orders:read" matches "orders:read"
 * - Domain wildcards: "orders:*" matches "orders:read", "orders:create", etc.
 * - Action wildcards: "*:read" matches "orders:read", "users:read", etc.
 * - System-wide: "*:*" matches everything (typically admin-only)
 *
 * Permission format is expected to be "domain:action" but the evaluator
 * is flexible and can work with any consistent string format.
 */
class DefaultPermissionEvaluator(
    private val permissionService: PermissionService
) : PermissionEvaluator {

    private val logger = LoggerFactory.getLogger(DefaultPermissionEvaluator::class.java)

    override fun hasPermission(userId: UUID, permission: String): Boolean {
        logger.debug("Evaluating permission '$permission' for user: $userId")

        val userPermissions = permissionService.getUserPermissions(userId)
        val hasPermission = matchesPermission(userPermissions, permission)

        if (!hasPermission) {
            logger.debug("Permission '$permission' denied for user: $userId - Available permissions: $userPermissions")
        } else {
            logger.debug("Permission '$permission' granted for user: $userId")
        }

        return hasPermission
    }

    /**
     * Check if user permissions match required permission.
     * Supports wildcard matching for flexible permission schemes.
     */
    private fun matchesPermission(userPermissions: Set<String>, requiredPermission: String): Boolean {
        // System-wide permissions (admin access)
        if (userPermissions.contains("*:*")) {
            return true
        }

        // Direct match
        if (userPermissions.contains(requiredPermission)) {
            return true
        }

        // Parse permission for wildcard matching
        // Expected format: "domain:action", but gracefully handle other formats
        val parts = requiredPermission.split(":", limit = 2)
        if (parts.size != 2) {
            // If permission format doesn't match expected pattern, only direct match works
            return false
        }

        val domain = parts[0]
        val action = parts[1]

        // Wildcard domain matching: "domain:*" matches "domain:action"
        val wildcardActionPermission = "$domain:*"
        if (userPermissions.contains(wildcardActionPermission)) {
            return true
        }

        // Wildcard action matching: "*:action" matches "domain:action"
        val wildcardDomainPermission = "*:$action"
        if (userPermissions.contains(wildcardDomainPermission)) {
            return true
        }

        return false
    }
}