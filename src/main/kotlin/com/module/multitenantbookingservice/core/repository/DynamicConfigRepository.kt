package com.module.multitenantbookingservice.core.repository

import com.module.multitenantbookingservice.core.models.DynamicConfig
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID


@Repository
interface DynamicConfigRepository: CrudRepository<DynamicConfig, UUID> {
    fun findByTenantIdAndKey(tenantId: UUID, key: String): Optional<DynamicConfig>
}