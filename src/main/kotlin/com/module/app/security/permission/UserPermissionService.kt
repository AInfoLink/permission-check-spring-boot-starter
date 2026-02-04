package com.module.app.security.permission

import java.util.*


interface PermissionEvaluator {
    fun hasPermission(
        userId: UUID,
        permission: String
    ): Boolean
}

interface UserPermissionService {
    fun getUserPermissions(userId: UUID): Set<String>
    fun evictUserPermissions(userId: UUID)
}
