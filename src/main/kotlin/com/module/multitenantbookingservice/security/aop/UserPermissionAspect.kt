package com.module.multitenantbookingservice.security.aop

import com.module.multitenantbookingservice.security.annotation.Require
import com.module.multitenantbookingservice.security.annotation.extractPermissions
import com.module.multitenantbookingservice.security.permission.PermissionEvaluator
import com.module.multitenantbookingservice.security.model.User
import com.module.multitenantbookingservice.utils.Safe
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

data class PermissionDeniedException(
    val userId: UUID,
    val requiredPermissions: String,
    val methodName: String
) : Exception(
    "Permission denied for user=$userId. Required permission(s): $requiredPermissions"
)

@Aspect
@Component
class UserPermissionAspect(
    private val permissionEvaluator: PermissionEvaluator
) {
    private val logger = LoggerFactory.getLogger(UserPermissionAspect::class.java)

    @Around("@annotation(require)")
    fun around(
        joinPoint: ProceedingJoinPoint,
        require: Require
    ): Any? {
        val methodName = (joinPoint.signature as MethodSignature).method.name
        val className = joinPoint.target.javaClass.simpleName

        val currentUserId = getCurrentUserId()
            ?: throw SecurityException("User not authenticated")

        logger.debug("Permission check initiated for user=$currentUserId method=$className.$methodName")

        validatePermissions(currentUserId, require, methodName)
        logger.debug("Permission granted for user=$currentUserId method=$className.$methodName")

        return joinPoint.proceed()
    }

    private fun validatePermissions(userId: UUID, require: Require, methodName: String) {
        val hasPermission = checkPermissions(userId, require)
        if (hasPermission) {
            return
        }
        val permissionsInfo = getPermissionsContext(require)
        logger.warn("Permission denied for user=$userId method=$methodName. Required: $permissionsInfo")

        throw PermissionDeniedException(
            userId = userId,
            requiredPermissions = permissionsInfo,
            methodName = methodName
        )
    }

    /**
     * Check user permissions (simplified version - does not handle resource owner validation)
     */
    private fun checkPermissions(userId: UUID, require: Require): Boolean {
        val permissionsToCheck = require.extractPermissions()

        return when (require.requireAll) {
            true -> {
                // AND logic - requires all permissions
                for (permission in permissionsToCheck) {
                    if (!permissionEvaluator.hasPermission(userId, permission)) {
                        return false
                    }
                }
                true
            }
            false -> {
                // OR logic - any permission is sufficient
                for (permission in permissionsToCheck) {
                    if (permissionEvaluator.hasPermission(userId, permission)) {
                        return true
                    }
                }
                false
            }
        }
    }

    /**
     * Get permission information string (for logging and error messages)
     */
    private fun getPermissionsContext(require: Require): String {
        val permissionsToCheck = require.extractPermissions()

        return when {
            permissionsToCheck.size > 1 -> {
                val operator = require.getOperatorString()
                permissionsToCheck.joinToString(" $operator ")
            }
            else -> permissionsToCheck.first()
        }
    }

    private fun Require.getOperatorString(): String =
        when (requireAll) {
            true -> "AND"
            false -> "OR"
        }

    /**
     * Get current user ID from Spring Security Context
     */
    private fun getCurrentUserId(): UUID? {
        return Safe.call(logger) {
            logger.debug("Retrieving current user ID from security context")
            val context = SecurityContextHolder.getContext()
            val authentication = context.authentication

            if (authentication == null) {
                logger.warn("No authentication found in security context")
                return@call null
            }

            val principal = authentication.principal
            val user = principal as? User

            if (user == null) {
                logger.warn("Principal is not a User instance: ${principal?.javaClass?.simpleName}")
                return@call null
            }
            user.id
        }
    }
}