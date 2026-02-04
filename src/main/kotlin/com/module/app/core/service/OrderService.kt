package com.module.app.core.service

import com.module.app.core.models.*
import com.module.app.core.repository.OrderIdentityRepository
import com.module.app.core.repository.OrderRepository
import com.module.app.security.OrderIdentityAlreadyExists
import com.module.app.security.OrderIdentityNotFound
import com.module.app.security.OrderNotFound
import com.module.app.security.UserNotFound
import com.module.app.security.annotation.Permission
import com.module.app.security.annotation.Require
import com.module.app.security.repository.UserRepository
import com.module.app.security.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class OrderIdentityCreation(
    val type: IdentityType,
    val email: String,
    val name: String,
    val userId: UUID? = null
)

data class OrderItemCreation(
    val description: String,
    val amount: Int,
    val referenceType: ReferenceType,
    val referenceId: UUID
)

data class OrderCreation(
    val description: String,
    val amount: Int,
    val identityId: UUID,
    val items: List<OrderItemCreation> = emptyList()
)

data class OrderUpdate(
    val description: String? = null,
    val amount: Int? = null,
    val paymentStatus: PaymentStatus? = null
)

data class PaymentStatusUpdate(
    val status: PaymentStatus
)

data class OrderQuery(
    val identityId: UUID? = null,
    val email: String? = null,
    val paymentStatus: PaymentStatus? = null
)

interface OrderService {
    fun createOrderIdentity(identity: OrderIdentityCreation): OrderIdentity
    // Order operations
    fun createOrder(order: OrderCreation): Order
    fun getOrderById(orderId: UUID): Order
    fun searchOrders(query: OrderQuery, pageable: Pageable): Page<Order>
    fun updateOrder(orderId: UUID, update: OrderUpdate): Order
    fun updatePaymentStatus(orderId: UUID, update: PaymentStatusUpdate): Order
    fun deleteOrder(orderId: UUID)

    fun getAllOrders(): List<Order>
}

@Service
class DefaultOrderService(
    private val orderRepository: OrderRepository,
    private val orderIdentityRepository: OrderIdentityRepository,
    private val userRepository: UserRepository
): OrderService {

    private val logger = LoggerFactory.getLogger(DefaultOrderService::class.java)

    /**
     * Create order identity with optional association to existing user
     */
    @Require(Permission.ORDERS_CREATE)
    @Transactional
    override fun createOrderIdentity(identity: OrderIdentityCreation): OrderIdentity {
        logger.info("Attempting to create order identity for email: ${identity.email}, type: ${identity.type}")

        // Check if identity with same email and type already exists
        orderIdentityRepository.findByEmailAndType(identity.email, identity.type).getOrNull()?.let {
            logger.warn("Order identity creation failed - duplicate found for email: ${identity.email}, type: ${identity.type}")
            throw OrderIdentityAlreadyExists
        }

        val user: User? = identity.userId?.let { userId ->
            logger.debug("Linking order identity to existing user: $userId")
            userRepository.findById(userId).getOrNull() ?: run {
                logger.error("User not found for ID: $userId during order identity creation")
                throw UserNotFound
            }
        }

        val orderIdentity = OrderIdentity(
            type = identity.type,
            email = identity.email,
            name = identity.name,
            user = user,
            createdAt = Instant.now()
        )

        val savedIdentity = orderIdentityRepository.save(orderIdentity)
        logger.info("Order identity created successfully - ID: ${savedIdentity.id}, email: ${savedIdentity.email}")
        return savedIdentity
    }

    /**
     * Create order
     */
    @Require(Permission.ORDERS_CREATE)
    @Transactional
    override fun createOrder(order: OrderCreation): Order {
        logger.info("Creating order for identity: ${order.identityId}, amount: ${order.amount}, items count: ${order.items.size}")

        val identity = orderIdentityRepository.findById(order.identityId).getOrNull() ?: run {
            logger.error("Order creation failed - identity not found: ${order.identityId}")
            throw OrderIdentityNotFound
        }

        val newOrder = Order(
            description = order.description,
            amount = order.amount,
            identity = identity,
            paymentStatus = PaymentStatus.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        // Create order items
        order.items.forEach { itemData ->
            logger.debug("Processing order item - reference type: ${itemData.referenceType}, reference ID: ${itemData.referenceId}, amount: ${itemData.amount}")

            val orderItem = OrderItem(
                description = itemData.description,
                amount = itemData.amount,
                order = newOrder,
                referenceType = itemData.referenceType,
                referenceId = itemData.referenceId,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            newOrder.items.add(orderItem)
        }

        val savedOrder = orderRepository.save(newOrder)  // Cascade will automatically save items
        logger.info("Order created successfully - ID: ${savedOrder.id}, total amount: ${savedOrder.amount}, items count: ${savedOrder.items.size}")
        return savedOrder
    }

    /**
     * Query order by ID
     */
    @Require(Permission.ORDERS_READ)
    @Transactional(readOnly = true)
    override fun getOrderById(orderId: UUID): Order {
        logger.debug("Retrieving order by ID: $orderId")
        return orderRepository.findById(orderId).getOrNull() ?: run {
            logger.warn("Order not found for ID: $orderId")
            throw OrderNotFound
        }
    }

    /**
     * Unified order search method supporting multiple query criteria and pagination
     */
    @Require(Permission.ORDERS_READ)
    @Transactional(readOnly = true)
    override fun searchOrders(query: OrderQuery, pageable: Pageable): Page<Order> {
        return orderRepository.findOrdersWithCriteria(
            identityId = query.identityId,
            email = query.email,
            paymentStatus = query.paymentStatus,
            pageable = pageable
        )
    }

    /**
     * Query all orders
     */
    @Require(Permission.ORDERS_READ)
    @Transactional(readOnly = true)
    override fun getAllOrders(): List<Order> {
        return orderRepository.findAll()
    }

    /**
     * Update order information
     */
    @Require(Permission.ORDERS_UPDATE)
    @Transactional
    override fun updateOrder(orderId: UUID, update: OrderUpdate): Order {
        val order = orderRepository.findById(orderId).getOrNull() ?: throw OrderNotFound

        update.description?.let {
            order.description = it
        }
        update.amount?.let {
            order.amount = it
        }
        update.paymentStatus?.let {
            order.paymentStatus = it
        }

        return orderRepository.save(order)
    }

    /**
     * Update order payment status
     */
    @Require(Permission.ORDERS_UPDATE)
    @Transactional
    override fun updatePaymentStatus(orderId: UUID, update: PaymentStatusUpdate): Order {
        logger.info("Updating payment status for order: $orderId to status: ${update.status}")

        val order = orderRepository.findById(orderId).getOrNull() ?: run {
            logger.error("Payment status update failed - order not found: $orderId")
            throw OrderNotFound
        }

        val previousStatus = order.paymentStatus
        order.paymentStatus = update.status
        val updatedOrder = orderRepository.save(order)

        logger.info("Payment status updated successfully - order: $orderId, from: $previousStatus to: ${update.status}")
        return updatedOrder
    }

    /**
     * Delete order
     */
    @Require(Permission.ORDERS_DELETE)
    @Transactional
    override fun deleteOrder(orderId: UUID) {
        logger.info("Attempting to delete order: $orderId")

        val order = orderRepository.findById(orderId).getOrNull()
        if (order == null) {
            logger.warn("Order deletion requested but order not found: $orderId")
            return
        }

        try {
            orderRepository.delete(order)
            logger.info("Order deleted successfully - ID: $orderId, amount: ${order.amount}")
        } catch (ex: Exception) {
            logger.error("Failed to delete order: $orderId", ex)
            throw ex
        }
    }


}