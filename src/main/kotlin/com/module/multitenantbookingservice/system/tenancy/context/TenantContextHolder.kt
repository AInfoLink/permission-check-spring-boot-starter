package com.module.multitenantbookingservice.system.tenancy.context

import org.slf4j.LoggerFactory

/**
 * Tenant Context Holder - Singleton Object
 *
 * Responsibilities:
 * 1. Store tenant ID in the current thread
 * 2. Support tenant context passing between threads
 * 3. Provide tenant context cleanup
 */
object TenantContextHolder {

    private const val COMMON_SCHEMA = "PUBLIC"

    private val logger = LoggerFactory.getLogger(TenantContextHolder::class.java)
    private val contextHolder: ThreadLocal<String> = InheritableThreadLocal()

    fun setTenantId(tenantId: String?) {
        logger.debug("Setting tenant ID in context: $tenantId")
        contextHolder.set(tenantId)
    }

    fun getTenantId(): String? {
        return contextHolder.get()
    }

    fun clear() {
        logger.debug("Clearing tenant ID from context")
        contextHolder.remove()
    }
}