package com.module.multitenantbookingservice.core.repository

import com.module.multitenantbookingservice.core.models.Order
import com.module.multitenantbookingservice.core.models.OrderIdentity
import com.module.multitenantbookingservice.core.models.PaymentStatus
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository : ListCrudRepository<Order, UUID> {
    fun findByIdentity(identity: OrderIdentity): List<Order>
    fun findByPaymentStatus(paymentStatus: PaymentStatus): List<Order>
    fun findByIdentityEmail(email: String): List<Order>
    fun findByIdentityEmailAndPaymentStatus(email: String, paymentStatus: PaymentStatus): List<Order>
}