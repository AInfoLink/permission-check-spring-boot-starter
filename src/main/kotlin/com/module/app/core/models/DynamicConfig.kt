package com.module.app.core.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

/**
 * Entity for storing dynamic configuration data with flexible JSON body.
 *
 * The `body` field stores configuration data as a JSON object that can be
 * converted to strongly typed objects using [ConfigMapperService].
 *
 * ## Usage with ConfigMapperService
 *
 * To convert the `body` to a typed object, use `ConfigMapperService`:
 *
 * ```kotlin
 * @Service
 * class MyService(private val configMapper: ConfigMapperService) {
 *
 *     data class DatabaseConfig(val host: String, val port: Int)
 *
 *     fun processConfig(config: DynamicConfig) {
 *         // Convert to typed object
 *         val dbConfig = configMapper.convert(config, DatabaseConfig::class.java)
 *
 *         // Or use reified generics
 *         val dbConfig = convertConfig<DatabaseConfig>(config, configMapper)
 *     }
 * }
 * ```
 *
 * ## Database Schema
 *
 * The `body` field is stored as JSON in the database using Hibernate's
 * `@JdbcTypeCode(SqlTypes.JSON)` annotation.
 *
 * @property id Unique identifier for this config entry
 * @property key Configuration key (e.g., "database.config", "feature.flags")
 * @property body Configuration data as JSON object stored as Map<String, Any>
 *
 * @see ConfigMapperService For converting body to typed objects
 * @since 1.0.0
 */
@Entity
@Table(name = "dynamic_configs")
class DynamicConfig(
    @Id
    val id: UUID = UUID.randomUUID(),
    val key: String,

    @Column(name = "tenant_id")
    var tenantId: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    val body: Map<String, Any>
)