package com.module.multitenantbookingservice.core.config

import com.module.multitenantbookingservice.core.repository.DynamicConfigRepository
import com.module.multitenantbookingservice.system.tenancy.context.TenantContextHolder
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

interface DynamicConfigRetriever<T> {
    fun getConfig(tenantId: UUID): T
    fun getConfig(): T {
        val tenantId = TenantContextHolder.getTenantId() ?: throw IllegalStateException("Tenant ID not found in context")
        return getConfig(tenantId)
    }
}

/**
 * Generic configuration retriever that eliminates the need for specific retriever classes.
 *
 * This service provides a unified way to retrieve any type of configuration from the
 * DynamicConfig repository, reducing code duplication and simplifying configuration management.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * @Service
 * class MyService(private val configRetriever: GenericConfigRetriever) {
 *
 *     fun getBookingConfig(tenantId: UUID): BookingTimeSlotConfig {
 *         return configRetriever.getConfig(
 *             tenantId = tenantId,
 *             configKey = BookingTimeSlotConfig.CONFIG_KEY,
 *             configClass = BookingTimeSlotConfig::class.java
 *         ) { BookingTimeSlotConfig().withDefaultConfig(TimeSlotInterval.HOURLY) }
 *     }
 * }
 * ```
 *
 * @param dynamicConfigRepository Repository for accessing dynamic configurations
 * @param mapper Service for converting JSON config body to typed objects
 */
@Service
class GenericConfigRetriever(
    private val dynamicConfigRepository: DynamicConfigRepository,
    private val mapper: ConfigMapperService
) {

    /**
     * Retrieves a configuration of the specified type for the given tenant.
     *
     * @param tenantId The tenant identifier
     * @param configKey The configuration key to look up
     * @param configClass The target configuration class
     * @param defaultProvider Lambda function to provide default configuration when none exists
     * @return The configuration instance, either from database or default
     * @throws IllegalArgumentException if configuration conversion fails
     */
    fun <T> getConfig(
        tenantId: UUID,
        configKey: String,
        configClass: Class<T>,
        defaultProvider: () -> T
    ): T {
        val dynamicConfig = dynamicConfigRepository.findByTenantIdAndKey(tenantId, configKey).getOrNull()
            ?: return defaultProvider()
        return mapper.convert(dynamicConfig, configClass)
    }

    /**
     * Tenant-aware version using TenantContextHolder.
     * Automatically retrieves tenant ID from current context.
     *
     * @param configKey The configuration key to look up
     * @param configClass The target configuration class
     * @param defaultProvider Optional lambda function to provide default configuration when none exists
     * @return The configuration instance, either from database or default
     * @throws IllegalStateException if tenant ID not found in context
     * @throws IllegalArgumentException if configuration conversion fails or no config found and no default provided
     */
    fun <T> getConfig(
        configKey: String,
        configClass: Class<T>,
        defaultProvider: (() -> T)? = null
    ): T {
        val tenantId = TenantContextHolder.getTenantId()
            ?: throw IllegalStateException("Tenant ID not found in context")

        val dynamicConfig = dynamicConfigRepository.findByTenantIdAndKey(tenantId, configKey).getOrNull()

        return if (dynamicConfig != null) {
            mapper.convert(dynamicConfig, configClass)
        } else if (defaultProvider != null) {
            defaultProvider()
        } else {
            throw IllegalArgumentException("Configuration not found for key: $configKey and tenantId: $tenantId")
        }
    }
}