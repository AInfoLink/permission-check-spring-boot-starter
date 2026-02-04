package com.module.app.core.repository

import com.module.app.core.models.OrderItem
import com.module.app.core.models.ReferenceType
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderItemRepository : ListCrudRepository<OrderItem, UUID> {
    fun findByReferenceType(referenceType: ReferenceType): List<OrderItem>
    fun findByReferenceId(referenceId: UUID): List<OrderItem>
    fun findByReferenceTypeAndReferenceId(referenceType: ReferenceType, referenceId: UUID): Optional<OrderItem>
    fun findByDescriptionContaining(description: String): List<OrderItem>
}