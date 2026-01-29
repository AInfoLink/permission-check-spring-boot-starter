package com.module.multitenantbookingservice.system.tenancy.context

import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Tenant Context Holder - Singleton Object
 *
 * Responsibilities:
 * 1. Store tenant ID in the current thread
 * 2. Support tenant context passing between threads
 * 3. Provide tenant context cleanup
 *
 * Updated to use UUID for tenant IDs for type safety and consistency.
 */
object TenantContextHolder {

    private const val COMMON_SCHEMA = "PUBLIC"

    private val logger = LoggerFactory.getLogger(TenantContextHolder::class.java)
    private val contextHolder: ThreadLocal<UUID> = InheritableThreadLocal()

    /**
     * Set tenant ID as UUID
     */
    fun setTenantId(tenantId: UUID?) {
        logger.debug("Setting tenant ID in context: $tenantId")
        contextHolder.set(tenantId)
    }

    /**
     * Get tenant ID as UUID
     */
    fun getTenantId(): UUID? {
        return contextHolder.get()
    }

    fun clear() {
        logger.debug("Clearing tenant ID from context")
        contextHolder.remove()
    }
}