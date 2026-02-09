package io.github.common.permission.annotation

/**
 * Annotation for method-level permission checking.
 * This annotation is completely domain-agnostic and supports any permission string format.
 *
 * Examples:
 * ```
 * @Require("orders:read")                           // Single permission
 * @Require(permissions = ["users:create", "roles:assign"]) // Multiple permissions (AND logic by default)
 * @Require(permissions = ["admin:*", "manager:*"], requireAll = false) // OR logic
 * @Require("*:*")                                  // System admin permission
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Require(
    /**
     * Single permission string.
     * If both permission and permissions are specified, permissions takes precedence.
     * Default "*:*" means system-wide access (typically admin-only).
     */
    val permission: String = "*:*",

    /**
     * Multiple permission strings for complex permission requirements.
     * Takes precedence over the single permission parameter.
     */
    val permissions: Array<String> = [],

    /**
     * Controls logical operator for multiple permissions:
     * - true (default): AND logic - user must have ALL specified permissions
     * - false: OR logic - user must have ANY of the specified permissions
     *
     * This parameter is ignored when checking a single permission.
     */
    val requireAll: Boolean = true
)

/**
 * Extension function to extract permission strings from the annotation.
 * This standardizes how permissions are retrieved regardless of whether
 * single or multiple permissions are specified.
 */
fun Require.extractPermissions(): List<String> {
    return when {
        permissions.isNotEmpty() -> permissions.toList()
        else -> listOf(permission)
    }
}