package com.module.multitenantbookingservice.core.config

import com.module.multitenantbookingservice.core.models.DynamicConfig
import com.module.multitenantbookingservice.core.repository.DynamicConfigRepository
import com.module.multitenantbookingservice.system.tenancy.context.TenantContextHolder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

interface DynamicConfigRetriever<T> {
    fun getConfig(tenantId: String): T
    fun getConfig(): T {
        val tenantId = TenantContextHolder.getTenantId() ?: throw IllegalStateException("Tenant ID not found in context")
        return getConfig(tenantId)
    }

    fun saveConfig(tenantId: String, config: T)
    fun saveConfig(config: T) {
        val tenantId = TenantContextHolder.getTenantId() ?: throw IllegalStateException("Tenant ID not found in context")
        saveConfig(tenantId, config)
    }
}

/**
 * Generic configuration retriever and saver that eliminates the need for specific retriever classes.
 *
 * This service provides a unified way to retrieve and save any type of configuration from the
 * DynamicConfig repository, reducing code duplication and simplifying configuration management.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * @Service
 * class MyService(private val configRetriever: GenericConfigRetriever) {
 *
 *     // Retrieve configuration with default fallback
 *     fun getBookingConfig(tenantId: String): BookingTimeSlotConfig {
 *         return configRetriever.getConfig(
 *             tenantId = tenantId,
 *             configKey = BookingTimeSlotConfig.CONFIG_KEY,
 *             configClass = BookingTimeSlotConfig::class.java
 *         ) { BookingTimeSlotConfig().withDefaultConfig(TimeSlotInterval.HOURLY) }
 *     }
 *
 *     // Save configuration
 *     fun updateBookingConfig(tenantId: String, config: BookingTimeSlotConfig) {
 *         configRetriever.saveConfig(
 *             tenantId = tenantId,
 *             configKey = BookingTimeSlotConfig.CONFIG_KEY,
 *             config = config
 *         )
 *     }
 *
 *     // Tenant-aware operations (using TenantContextHolder)
 *     fun getCurrentTenantConfig(): BookingTimeSlotConfig {
 *         return configRetriever.getConfig(
 *             configKey = BookingTimeSlotConfig.CONFIG_KEY,
 *             configClass = BookingTimeSlotConfig::class.java
 *         ) { BookingTimeSlotConfig().withDefaultConfig(TimeSlotInterval.HOURLY) }
 *     }
 *
 *     fun saveCurrentTenantConfig(config: BookingTimeSlotConfig) {
 *         configRetriever.saveConfig(BookingTimeSlotConfig.CONFIG_KEY, config)
 *     }
 * }
 * ```
 *
 * @param dynamicConfigRepository Repository for accessing dynamic configurations
 * @param mapper Service for converting between JSON config body and typed objects
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
        tenantId: String,
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

    /**
     * Saves a configuration for the specified tenant.
     * If a configuration with the same key and tenant already exists, it will be updated.
     *
     * @param tenantId The tenant identifier
     * @param configKey The configuration key to save
     * @param config The configuration object to save
     * @throws IllegalArgumentException if configuration conversion fails
     */
    fun <T> saveConfig(
        tenantId: String,
        configKey: String,
        config: T
    ) {
        val existingConfig = dynamicConfigRepository.findByTenantIdAndKey(tenantId, configKey).getOrNull()
        val configToSave = if (existingConfig != null) {
            // Update existing config with new body
            DynamicConfig(
                id = existingConfig.id,
                key = configKey,
                tenantId = tenantId,
                body = mapper.objectToMap(config)
            )
        } else {
            // Create new config
            DynamicConfig(
                key = configKey,
                tenantId = tenantId,
                body = mapper.objectToMap(config)
            )
        }

        dynamicConfigRepository.save(configToSave)
    }

    /**
     * Tenant-aware version using TenantContextHolder.
     * Automatically retrieves tenant ID from current context and saves the configuration.
     *
     * @param configKey The configuration key to save
     * @param config The configuration object to save
     * @throws IllegalStateException if tenant ID not found in context
     * @throws IllegalArgumentException if configuration conversion fails
     */
    fun <T> saveConfig(
        configKey: String,
        config: T
    ) {
        val tenantId = TenantContextHolder.getTenantId()
            ?: throw IllegalStateException("Tenant ID not found in context")

        saveConfig(tenantId, configKey, config)
    }
}