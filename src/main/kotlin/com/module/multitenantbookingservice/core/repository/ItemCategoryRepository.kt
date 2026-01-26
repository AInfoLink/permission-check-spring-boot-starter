package com.module.multitenantbookingservice.core.repository

import com.module.multitenantbookingservice.core.models.CategoryType
import com.module.multitenantbookingservice.core.models.ItemCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ItemCategoryRepository : JpaRepository<ItemCategory, UUID> {

    fun findByCode(code: String): ItemCategory?

    fun findByTypeAndIsActiveTrue(type: CategoryType): List<ItemCategory>

    fun findByIsActiveTrueOrderBySortOrder(): List<ItemCategory>

    @Query("SELECT c FROM ItemCategory c WHERE c.type = 'SYSTEM_MANAGED' AND c.isActive = true")
    fun findSystemManagedCategories(): List<ItemCategory>

    @Query("SELECT c FROM ItemCategory c WHERE c.type = 'USER_MANAGED' AND c.isActive = true")
    fun findUserManagedCategories(): List<ItemCategory>

    fun existsByCode(code: String): Boolean
}