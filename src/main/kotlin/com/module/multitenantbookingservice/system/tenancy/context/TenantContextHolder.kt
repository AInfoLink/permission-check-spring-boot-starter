package com.module.multitenantbookingservice.system.tenancy.context

import org.slf4j.LoggerFactory


class TenantContextHolder {
    companion object {
        private val COMMON_SCHEMA = "PUBLIC"

        private val logger = LoggerFactory.getLogger(TenantContextHolder::class.java)
        private val contextHolder: ThreadLocal<String> = InheritableThreadLocal()

        fun setTenantId(tenantId: String?) {
            logger.debug("Setting tenant ID in context: $tenantId")
            contextHolder.set(tenantId ?: COMMON_SCHEMA )
        }

        fun getTenantId(): String {
            return contextHolder.get()
        }

        fun clear() {
            logger.debug("Clearing tenant ID from context")
            contextHolder.remove()
        }
    }
}