package com.module.app.core.repository

import com.module.app.core.models.TenantRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TenantRoleRepository : JpaRepository<TenantRole, UUID> {

    /**
     * 根據角色名稱查找
     */
    fun findByName(name: String): Optional<TenantRole>

    /**
     * 檢查角色名稱是否已存在
     */
    fun existsByName(name: String): Boolean
}