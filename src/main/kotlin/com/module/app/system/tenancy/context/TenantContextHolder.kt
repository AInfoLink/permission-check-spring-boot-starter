package com.module.app.system.tenancy.context

import org.slf4j.LoggerFactory

/**
 * Tenant Context Holder - Singleton Object
 *
 * Responsibilities:
 * 1. Store tenant ID in the current thread
 * 2. Support tenant context passing between threads
 * 3. Provide tenant context cleanup
 *
 * Uses String for tenant IDs for simplicity and consistency with external APIs.
 */
object TenantContextHolder {

    private const val COMMON_SCHEMA = "PUBLIC"

    private val logger = LoggerFactory.getLogger(TenantContextHolder::class.java)
    private val contextHolder: ThreadLocal<String> = InheritableThreadLocal()

    /**
     * Set tenant ID as String
     */
    fun setTenantId(tenantId: String?) {
        logger.debug("Setting tenant ID in context: $tenantId")
        contextHolder.set(tenantId)
    }

    /**
     * Get tenant ID as String
     */
    fun getTenantId(): String? {
        return contextHolder.get()
    }

    fun clear() {
        logger.debug("Clearing tenant ID from context")
        contextHolder.remove()
    }
}