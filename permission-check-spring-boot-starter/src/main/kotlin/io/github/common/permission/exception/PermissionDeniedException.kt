package io.github.common.permission.exception

import java.util.*

/**
 * Exception thrown when a user lacks required permissions to access a method.
 * This exception provides detailed context for debugging and security auditing.
 */
data class PermissionDeniedException(
    val userId: UUID,
    val requiredPermissions: String,
    val methodName: String
) : RuntimeException(
    "Permission denied for user=$userId. Required permission(s): $requiredPermissions"
)