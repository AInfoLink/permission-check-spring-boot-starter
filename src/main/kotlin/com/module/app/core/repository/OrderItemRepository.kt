package com.module.app.core.repository

import com.module.app.core.models.OrderItem
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderItemRepository : ListCrudRepository<OrderItem, UUID> {
    fun findByDescriptionContaining(description: String): List<OrderItem>
}