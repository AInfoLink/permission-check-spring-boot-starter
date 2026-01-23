package com.module.multitenantbookingservice.system.tenancy.context

import org.slf4j.LoggerFactory

/**
 * 租户上下文持有者 - 单例对象
 *
 * 职责：
 * 1. 在当前线程中保存租户ID
 * 2. 支持线程间的租户上下文传递
 * 3. 提供租户上下文的清理
 */
object TenantContextHolder {

    private const val COMMON_SCHEMA = "PUBLIC"

    private val logger = LoggerFactory.getLogger(TenantContextHolder::class.java)
    private val contextHolder: ThreadLocal<String> = InheritableThreadLocal()

    fun setTenantId(tenantId: String?) {
        logger.debug("Setting tenant ID in context: $tenantId")
        contextHolder.set(tenantId ?: COMMON_SCHEMA)
    }

    fun getTenantId(): String {
        return contextHolder.get() ?: COMMON_SCHEMA
    }

    fun clear() {
        logger.debug("Clearing tenant ID from context")
        contextHolder.remove()
    }
}