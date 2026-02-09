package io.github.common.permission.provider

/**
 * Interface for user models that are aware of their own permissions.
 * This follows Spring's naming convention (like ApplicationContextAware, BeanNameAware).
 *
 * This is the most straightforward way to integrate with the permission system.
 *
 * Example usage:
 * ```kotlin
 * @Entity
 * class User : UserDetails, PermissionAware {
 *     // ... other user properties
 *
 *     override fun getPermissions(): Set<String> {
 *         return roles.flatMap { it.permissions }.toSet()
 *     }
 * }
 * ```
 */
interface PermissionAware {
    /**
     * Get all permissions for this user.
     * @return Set of permission strings (format: "domain:action" or with wildcards)
     */
    fun getPermissions(): Set<String>
}