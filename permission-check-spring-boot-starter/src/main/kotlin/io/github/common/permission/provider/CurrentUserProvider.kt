package io.github.common.permission.provider

import java.util.*

interface WithPermissionIdentity {
    fun getPermissionIdentity(): UUID
}

/**
 * Interface for applications to provide current user context to permission system.
 * This abstraction allows the permission system to remain completely decoupled
 * from specific user models and security implementations.
 */
interface CurrentUserProvider {
    /**
     * Extract current user ID from security context.
     * @return User ID if authenticated, null if not authenticated
     */
    fun getCurrentUserId(): UUID
}