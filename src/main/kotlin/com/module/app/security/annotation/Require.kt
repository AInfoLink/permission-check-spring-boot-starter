package com.module.app.security.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Require(
    val permission: Permission = Permission.SYSTEM_ALL, // Single permission (backward compatibility)
    val permissions: Array<Permission> = [], // Multi-permission support
    val requireAll: Boolean = true // false=any permission allowed(OR), true=all permissions required(AND)
)

fun Require.extractPermissions(): List<String> {
    return when {
        permissions.isNotEmpty() -> permissions.map { it.toString() }
        else -> listOf(permission.toString())
    }
}