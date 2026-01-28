package com.module.multitenantbookingservice.security.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Require(
    val permission: Permission = Permission.SYSTEM_ALL, // 單一權限（向後兼容）
    val permissions: Array<Permission> = [], // 多權限支援
    val requireAll: Boolean = true // false=任一權限即可(OR)，true=需要所有權限(AND)
)

fun Require.extractPermissions(): List<String> {
    return when {
        permissions.isNotEmpty() -> permissions.map { it.toString() }
        else -> listOf(permission.toString())
    }
}