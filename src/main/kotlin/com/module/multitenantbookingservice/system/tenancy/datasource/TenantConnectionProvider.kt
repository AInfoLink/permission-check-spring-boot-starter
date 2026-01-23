package com.module.multitenantbookingservice.system.tenancy.datasource

import org.hibernate.cfg.AvailableSettings
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.stereotype.Component
import java.sql.Connection
import javax.sql.DataSource


@Component
class TenantConnectionProvider(
    val datasource: DataSource
): MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {
    private val COMMON_SCHEMA = "PUBLIC"
    override fun isUnwrappableAs(p0: Class<*>): Boolean {
        return datasource.isWrapperFor(p0)
    }

    override fun <T : Any?> unwrap(p0: Class<T>): T {
        return datasource.unwrap(p0)
    }

    override fun getAnyConnection(): Connection {
        return getConnection(COMMON_SCHEMA)
    }

    override fun releaseAnyConnection(connection: Connection) {
        connection.schema = COMMON_SCHEMA
        connection.close()
    }

    override fun supportsAggressiveRelease(): Boolean {
        return false
    }

    override fun releaseConnection(tenantId: String, connection: Connection) {
        connection.schema = COMMON_SCHEMA
        connection.close()
    }

    override fun getConnection(tenantId: String): Connection {
        datasource.connection.schema = tenantId
        return datasource.connection
    }

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties[AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER] = this
    }
}