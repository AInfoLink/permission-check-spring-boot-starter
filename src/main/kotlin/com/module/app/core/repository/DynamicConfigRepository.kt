package com.module.app.core.repository

import com.module.app.core.models.DynamicConfig
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID
@Repository
interface DynamicConfigRepository: CrudRepository<DynamicConfig, UUID> {
    fun findByTenantIdAndKey(tenantId: String, key: String): Optional<DynamicConfig>
}