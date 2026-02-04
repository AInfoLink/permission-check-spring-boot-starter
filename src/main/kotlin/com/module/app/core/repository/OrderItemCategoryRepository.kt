package com.module.app.core.repository

import com.module.app.core.models.CategoryType
import com.module.app.core.models.OrderItemCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface OrderItemCategoryRepository : JpaRepository<OrderItemCategory, UUID> {

    fun findByCode(code: String): OrderItemCategory?

    fun findByTypeAndIsActiveTrue(type: CategoryType): List<OrderItemCategory>

    fun findByIsActiveTrue(): List<OrderItemCategory>

    @Query("SELECT c FROM OrderItemCategory c WHERE c.type = 'SYSTEM_MANAGED' AND c.isActive = true")
    fun findSystemManagedCategories(): List<OrderItemCategory>

    @Query("SELECT c FROM OrderItemCategory c WHERE c.type = 'USER_MANAGED' AND c.isActive = true")
    fun findUserManagedCategories(): List<OrderItemCategory>

    fun existsByCode(code: String): Boolean
}