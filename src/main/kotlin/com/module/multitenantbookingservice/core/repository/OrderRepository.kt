package com.module.multitenantbookingservice.core.repository

import com.module.multitenantbookingservice.core.models.Order
import com.module.multitenantbookingservice.core.models.OrderIdentity
import com.module.multitenantbookingservice.core.models.PaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {

    @Query("""
        SELECT o FROM Order o
        WHERE (:identityId IS NULL OR o.identity.id = :identityId)
        AND (:email IS NULL OR o.identity.email = :email)
        AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
        ORDER BY o.createdAt DESC
    """)
    fun findOrdersWithCriteria(
        @Param("identityId") identityId: UUID?,
        @Param("email") email: String?,
        @Param("paymentStatus") paymentStatus: PaymentStatus?,
        pageable: Pageable
    ): Page<Order>
}