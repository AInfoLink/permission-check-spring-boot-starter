package com.module.multitenantbookingservice.core.config

import java.util.UUID

interface DynamicConfigRetriever<T> {
    fun getConfig(tenantId: UUID): T
}