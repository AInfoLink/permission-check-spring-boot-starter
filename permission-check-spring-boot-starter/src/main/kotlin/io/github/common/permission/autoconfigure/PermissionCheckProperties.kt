package io.github.common.permission.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for permission checking system.
 */
@ConfigurationProperties(prefix = "permission-check")
data class PermissionCheckProperties(
    /**
     * Cache configuration for permission data.
     */
    val cache: CacheProperties = CacheProperties(),

    /**
     * Logging configuration for permission system.
     */
    val logging: LoggingProperties = LoggingProperties()
) {
    data class CacheProperties(
        /**
         * Enable permission caching. Default: true
         */
        val enabled: Boolean = true,

        /**
         * Cache name for storing user permissions. Default: "userPermissions"
         */
        val cacheName: String = "userPermissions",

        /**
         * Time-to-live for cached permissions in seconds. Default: 300 (5 minutes)
         * Set to 0 to disable TTL.
         */
        val ttlSeconds: Long = 300
    )

    data class LoggingProperties(
        /**
         * Enable debug logging for permission evaluation. Default: false
         */
        val debugEnabled: Boolean = false,

        /**
         * Enable audit logging for permission denials. Default: true
         */
        val auditEnabled: Boolean = true
    )
}