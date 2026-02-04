package com.module.app.system.tenancy.datasource

import org.hibernate.cfg.AvailableSettings
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.stereotype.Component
import java.sql.Connection
import javax.sql.DataSource


@Component
class TenantConnectionProvider(
    val datasource: DataSource
): MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {
    private val logger = LoggerFactory.getLogger(TenantConnectionProvider::class.java)
    override fun isUnwrappableAs(p0: Class<*>): Boolean {
        return datasource.isWrapperFor(p0)
    }

    override fun <T : Any?> unwrap(p0: Class<T>): T {
        return datasource.unwrap(p0)
    }

    override fun getAnyConnection(): Connection {
        return getConnection("public")
    }

    override fun releaseAnyConnection(connection: Connection) {
        connection.createStatement().use { stmt ->
            stmt.execute("SET search_path TO public")
        }
        connection.close()
    }

    override fun supportsAggressiveRelease(): Boolean {
        return false
    }

    override fun releaseConnection(tenantId: String, connection: Connection) {
        connection.createStatement().use { stmt ->
            stmt.execute("SET search_path TO public")
        }
        connection.close()
    }

    override fun getConnection(tenantId: String): Connection {
        val connection = datasource.connection
        connection.createStatement().use { stmt ->
            // 設定 search_path
            stmt.execute("SET search_path TO $tenantId, public")
        }
        logger.info("Getting connection for tenant: $tenantId")
        return connection
    }

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties[AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER] = this
    }
}