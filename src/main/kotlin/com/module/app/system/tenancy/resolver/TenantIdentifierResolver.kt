package com.module.app.system.tenancy.resolver

 import com.module.app.system.tenancy.context.TenantContextHolder
import org.hibernate.cfg.AvailableSettings
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
 import org.springframework.stereotype.Component


@Component
class TenantIdentifierResolver: CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {
    override fun resolveCurrentTenantIdentifier(): String {
        return TenantContextHolder.getTenantId() ?: "PUBLIC"
    }

    override fun validateExistingCurrentSessions(): Boolean {
        return false
    }

    override fun customize(hibernateProperties: MutableMap<String, Any>) {
        hibernateProperties[AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER] = this
    }
}