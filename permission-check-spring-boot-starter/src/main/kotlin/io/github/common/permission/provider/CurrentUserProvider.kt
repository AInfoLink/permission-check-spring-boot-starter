package io.github.common.permission.provider

import java.util.*

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
    fun getCurrentUserId(): UUID?

    /**
     * Get additional user context information (optional, for advanced features).
     * This could include tenant ID, organization ID, department, etc.
     * The permission system doesn't use this directly but makes it available
     * for custom permission evaluation logic.
     *
     * @return Map of context key-value pairs (empty map by default)
     */
    fun getCurrentUserContext(): Map<String, Any> = emptyMap()
}