package com.module.multitenantbookingservice.core.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.module.multitenantbookingservice.core.models.DynamicConfig
import org.springframework.stereotype.Service

/**
 * Service for converting DynamicConfig body (Map<String, Any>) to strongly typed objects.
 *
 * This service replaces the deprecated DynamicConfigCastable interface and provides
 * type-safe conversion with proper error handling.
 *
 * ## Usage Examples
 *
 * ### 1. Basic conversion using Class parameter:
 * ```kotlin
 * @Service
 * class MyService(private val configMapper: ConfigMapperService) {
 *
 *     data class DatabaseConfig(val host: String, val port: Int)
 *
 *     fun processConfig(config: DynamicConfig) {
 *         val dbConfig = configMapper.convert(config, DatabaseConfig::class.java)
 *         // or
 *         val dbConfig = configMapper.mapToType(config.body, DatabaseConfig::class.java)
 *     }
 * }
 * ```
 *
 * ### 2. Type-safe conversion using reified generics:
 * ```kotlin
 * @Service
 * class MyService(private val configMapper: ConfigMapperService) {
 *
 *     fun processConfig(config: DynamicConfig) {
 *         val dbConfig = convertConfig<DatabaseConfig>(config, configMapper)
 *         // or
 *         val dbConfig = mapToType<DatabaseConfig>(config.body, configMapper)
 *     }
 * }
 * ```
 *
 * ### 3. Safe conversion with error handling:
 * ```kotlin
 * fun safeConvert(config: DynamicConfig): DatabaseConfig? {
 *     return try {
 *         configMapper.convert(config, DatabaseConfig::class.java)
 *     } catch (e: IllegalArgumentException) {
 *         logger.warn("Failed to convert config '${config.key}': ${e.message}")
 *         null
 *     }
 * }
 * ```
 *
 * ## Migration from DynamicConfigCastable
 *
 * ```
 *
 * ### New approach (recommended):
 * ```kotlin
 * val dbConfig = configMapper.convert(config, DatabaseConfig::class.java)  // ✅ Type-safe with proper error handling
 * ```
 *
 * @see DynamicConfig
 *
 */
@Service
class ConfigMapperService {
    private val mapper = jacksonObjectMapper()

    /**
     * Converts a Map to the specified type using Class parameter
     * @param body The map data to convert
     * @param clazz The target class type
     * @return The converted object of type T
     * @throws IllegalArgumentException if conversion fails
     */
    fun <T> mapToType(body: Map<String, Any>, clazz: Class<T>): T {
        return try {
            // Convert Map to JSON string, then parse to target type
            val jsonString = mapper.writeValueAsString(body)
            mapper.readValue(jsonString, clazz)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to convert config body to type ${clazz.simpleName}", e)
        }
    }

    /**
     * Converts a DynamicConfig's body to the specified type using Class parameter
     * @param config The DynamicConfig instance
     * @param clazz The target class type
     * @return The converted object of type T
     */
    fun <T> convert(config: DynamicConfig, clazz: Class<T>): T {
        return mapToType(config.body, clazz)
    }

    /**
     * Converts an object to a Map<String, Any> for DynamicConfig body storage
     * @param obj The object to convert
     * @return The converted Map representation
     * @throws IllegalArgumentException if conversion fails
     */
    fun <T> objectToMap(obj: T): Map<String, Any> {
        return try {
            // Convert object to JSON string, then parse to Map
            val jsonString = mapper.writeValueAsString(obj)
            mapper.readValue(jsonString, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to convert object ${obj!!::class.java} to map", e)
        }
    }
}

// =============================================================================
// Extension Functions for Reified Generic Support
// =============================================================================

/**
 * Extension functions for type-safe conversion using reified generics.
 * These are top-level functions to enable inline/reified usage which
 * cannot be used directly in service class methods.
 *
 * ## Why Extension Functions?
 *
 * Kotlin's `inline` functions with `reified` generics cannot be virtual methods
 * in classes, so we provide these as top-level extension functions for better
 * type safety and convenience.
 *
 * ## Usage
 *
 * Instead of:
 * ```kotlin
 * val config = configMapper.convert(dynamicConfig, DatabaseConfig::class.java)
 * ```
 *
 * You can use:
 * ```kotlin
 * val config = convertConfig<DatabaseConfig>(dynamicConfig, configMapper)
 * ```
 */

/**
 * Converts a Map to the specified type T using reified generics.
 *
 * @param T The target type (automatically inferred)
 * @param body The map data to convert
 * @param mapperService The mapper service instance
 * @return The converted object of type T
 * @throws IllegalArgumentException if conversion fails
 *
 * @since 1.0.0
 */
inline fun <reified T> mapToType(body: Map<String, Any>, mapperService: ConfigMapperService): T {
    return mapperService.mapToType(body, T::class.java)
}

/**
 * Converts a DynamicConfig's body to the specified type T using reified generics.
 *
 * This is the preferred method for converting DynamicConfig instances to typed objects
 * when you want compile-time type safety.
 *
 * @param T The target type (automatically inferred)
 * @param config The DynamicConfig instance
 * @param mapperService The mapper service instance
 * @return The converted object of type T
 * @throws IllegalArgumentException if conversion fails
 *
 * @since 1.0.0
 */
inline fun <reified T> convertConfig(config: DynamicConfig, mapperService: ConfigMapperService): T {
    return mapperService.convert(config, T::class.java)
}

// =============================================================================
// Extension Functions for Config Saving with Generic Support
// =============================================================================

/**
 * Extension function to save configuration with reified generics support.
 * This provides a type-safe way to save configurations without explicitly passing Class objects.
 *
 * Usage:
 * ```kotlin
 * // Instead of:
 * configRetriever.saveConfig(tenantId, "my.config.key", myConfig)
 *
 * // You can use:
 * saveConfig<MyConfig>(tenantId, "my.config.key", myConfig, configRetriever)
 * ```
 *
 * @param T The configuration type (automatically inferred)
 * @param tenantId The tenant identifier
 * @param configKey The configuration key
 * @param config The configuration object to save
 * @param configRetriever The GenericConfigRetriever instance
 */
inline fun <reified T> saveConfig(
    tenantId: java.util.UUID,
    configKey: String,
    config: T,
    configRetriever: GenericConfigRetriever
) {
    configRetriever.saveConfig(tenantId, configKey, config)
}

/**
 * Extension function to save configuration with tenant context using reified generics.
 * This provides a type-safe way to save configurations using TenantContextHolder.
 *
 * Usage:
 * ```kotlin
 * // Instead of:
 * configRetriever.saveConfig("my.config.key", myConfig)
 *
 * // You can use:
 * saveConfig<MyConfig>("my.config.key", myConfig, configRetriever)
 * ```
 *
 * @param T The configuration type (automatically inferred)
 * @param configKey The configuration key
 * @param config The configuration object to save
 * @param configRetriever The GenericConfigRetriever instance
 * @throws IllegalStateException if tenant ID not found in context
 */
inline fun <reified T> saveConfig(
    configKey: String,
    config: T,
    configRetriever: GenericConfigRetriever
) {
    configRetriever.saveConfig(configKey, config)
}