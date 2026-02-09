package io.github.common.permission.autoconfigure

import io.github.common.permission.provider.CurrentUserProvider
import io.github.common.permission.provider.PermissionRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Import

/**
 * Auto-configuration that activates when AspectJ is available.
 * Provides sensible defaults for CurrentUserProvider and PermissionRepository,
 * but allows applications to override with custom implementations.
 */
@AutoConfiguration
@ConditionalOnClass(name = ["org.aspectj.lang.annotation.Aspect"])
@Import(PermissionCheckConfiguration::class)
class PermissionCheckAutoConfiguration