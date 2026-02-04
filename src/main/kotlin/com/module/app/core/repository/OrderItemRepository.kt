package com.module.app.core.repository

import com.module.app.core.models.OrderItemCategory
import com.module.app.core.models.OrderItem
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderItemRepository : ListCrudRepository<OrderItem, UUID> {
    fun findByCategory(category: OrderItemCategory): List<OrderItem>
    fun findByCategoryId(categoryId: UUID): List<OrderItem>
    fun findByDescriptionContaining(description: String): List<OrderItem>
}