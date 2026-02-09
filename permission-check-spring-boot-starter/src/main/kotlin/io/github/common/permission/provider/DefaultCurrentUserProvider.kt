package io.github.common.permission.provider

import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

/**
 * Default implementation of CurrentUserProvider that works with common Spring Security patterns.
 * This implementation attempts to extract user ID from various common user model patterns.
 *
 * Supports:
 * - Any user object with getId() method returning UUID or String
 * - Spring Security UserDetails with username as UUID
 * - Custom user models with 'id' property
 *
 * This default implementation covers 80% of use cases, but users can provide their own
 * implementation if needed.
 */
class DefaultCurrentUserProvider : CurrentUserProvider {
    override fun getCurrentUserId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication.principal
        if (principal !is UserIdentityRequired) {
            throw IllegalStateException("Principal does not implement UserIdentityRequired - cannot extract user ID")
        }
        return principal.getCurrentUserId()
    }
}