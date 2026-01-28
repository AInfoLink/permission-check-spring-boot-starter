package com.module.multitenantbookingservice.security.aop

import com.module.multitenantbookingservice.security.annotation.Require
import com.module.multitenantbookingservice.security.annotation.extractPermissions
import com.module.multitenantbookingservice.security.permission.PermissionEvaluator
import com.module.multitenantbookingservice.security.repository.model.User
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
        val currentUserId = getCurrentUserId()
            ?: throw SecurityException("User not authenticated")

        validatePermissions(currentUserId, require, methodName)
        logger.info("Permission granted for user=$currentUserId method=$methodName")

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
     * 檢查使用者權限（簡化版 - 不處理資源擁有者檢查）
     */
    private fun checkPermissions(userId: UUID, require: Require): Boolean {
        val permissionsToCheck = require.extractPermissions()

        return when (require.requireAll) {
            true -> {
                // AND 邏輯 - 需要所有權限
                for (permission in permissionsToCheck) {
                    if (!permissionEvaluator.hasPermission(userId, permission)) {
                        return false
                    }
                }
                true
            }
            false -> {
                // OR 邏輯 - 任一權限即可
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
     * 取得權限資訊字串（用於日誌和錯誤訊息）
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
     * 從 Spring Security Context 取得當前使用者ID
     */
    private fun getCurrentUserId(): UUID? {
        return try {
            val context = SecurityContextHolder.getContext()
            val authentication = context.authentication ?: return null
            val principal = authentication.principal ?: return null
            val user = principal as? User ?: return null
            user.id
        } catch (ex: Exception) {
            null
        }
    }
}