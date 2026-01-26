package com.module.multitenantbookingservice.core.service

import com.module.multitenantbookingservice.core.models.*
import com.module.multitenantbookingservice.core.repository.OrderIdentityRepository
import com.module.multitenantbookingservice.core.repository.OrderRepository
import com.module.multitenantbookingservice.security.*
import com.module.multitenantbookingservice.security.repository.UserRepository
import com.module.multitenantbookingservice.security.repository.model.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class OrderIdentityCreation(
    val type: IdentityType,
    val email: String,
    val name: String,
    val userId: UUID? = null
)

data class OrderCreation(
    val description: String,
    val amount: Int,
    val identityId: UUID,
    val items: List<UUID> = emptyList()
)

data class OrderUpdate(
    val description: String? = null,
    val amount: Int? = null,
    val paymentStatus: PaymentStatus? = null
)

interface OrderService {
    fun createOrderIdentity(identity: OrderIdentityCreation): OrderIdentity
    // Order operations
    fun createOrder(order: OrderCreation): Order
    fun getOrderById(orderId: UUID): Order
    fun getOrdersByIdentity(identityId: UUID): List<Order>
    fun getOrdersByEmail(email: String): List<Order>
    fun getOrdersByPaymentStatus(status: PaymentStatus): List<Order>
    fun getOrdersByEmailAndStatus(email: String, status: PaymentStatus): List<Order>
    fun getAllOrders(): List<Order>
    fun updateOrder(orderId: UUID, update: OrderUpdate): Order
    fun updatePaymentStatus(orderId: UUID, status: PaymentStatus): Order
    fun deleteOrder(orderId: UUID)
}

@Service
class DefaultOrderService(
    private val orderRepository: OrderRepository,
    private val orderIdentityRepository: OrderIdentityRepository,
    private val userRepository: UserRepository
): OrderService {

    /**
     * 創建訂單身份，可選擇關聯已存在的用戶
     */
    @Transactional
    override fun createOrderIdentity(identity: OrderIdentityCreation): OrderIdentity {
        // 檢查是否已經存在相同 email 和 type 的身份
        orderIdentityRepository.findByEmailAndType(identity.email, identity.type).getOrNull()?.let {
            throw OrderIdentityAlreadyExists
        }

        val user: User? = identity.userId?.let { userId ->
            userRepository.findById(userId).getOrNull() ?: throw UserNotFound
        }

        val orderIdentity = OrderIdentity(
            type = identity.type,
            email = identity.email,
            name = identity.name,
            user = user,
            createdAt = Instant.now()
        )

        return orderIdentityRepository.save(orderIdentity)
    }

    /**
     * 創建訂單
     */
    @Transactional
    override fun createOrder(order: OrderCreation): Order {
        val identity = orderIdentityRepository.findById(order.identityId).getOrNull()
            ?: throw OrderIdentityNotFound

        val newOrder = Order(
            description = order.description,
            amount = order.amount,
            identity = identity,
            paymentStatus = PaymentStatus.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        return orderRepository.save(newOrder)
    }

    /**
     * 查詢訂單
     */
    @Transactional(readOnly = true)
    override fun getOrderById(orderId: UUID): Order {
        return orderRepository.findById(orderId).getOrNull() ?: throw OrderNotFound
    }

    /**
     * 根據訂單身份查詢所有訂單
     */
    @Transactional(readOnly = true)
    override fun getOrdersByIdentity(identityId: UUID): List<Order> {
        val identity = orderIdentityRepository.findById(identityId).getOrNull()
            ?: throw OrderIdentityNotFound
        return orderRepository.findByIdentity(identity)
    }

    /**
     * 根據 email 查詢所有訂單
     */
    @Transactional(readOnly = true)
    override fun getOrdersByEmail(email: String): List<Order> {
        return orderRepository.findByIdentityEmail(email)
    }

    /**
     * 根據支付狀態查詢訂單
     */
    @Transactional(readOnly = true)
    override fun getOrdersByPaymentStatus(status: PaymentStatus): List<Order> {
        return orderRepository.findByPaymentStatus(status)
    }

    /**
     * 根據 email 和支付狀態查詢訂單
     */
    @Transactional(readOnly = true)
    override fun getOrdersByEmailAndStatus(email: String, status: PaymentStatus): List<Order> {
        return orderRepository.findByIdentityEmailAndPaymentStatus(email, status)
    }

    /**
     * 查詢所有訂單
     */
    @Transactional(readOnly = true)
    override fun getAllOrders(): List<Order> {
        return orderRepository.findAll()
    }

    /**
     * 更新訂單信息
     */
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
     * 更新訂單支付狀態
     */
    @Transactional
    override fun updatePaymentStatus(orderId: UUID, status: PaymentStatus): Order {
        val order = orderRepository.findById(orderId).getOrNull() ?: throw OrderNotFound
        order.paymentStatus = status
        return orderRepository.save(order)
    }

    /**
     * 刪除訂單
     */
    @Transactional
    override fun deleteOrder(orderId: UUID) {
        val order = orderRepository.findById(orderId).getOrNull() ?: return
        orderRepository.delete(order)
    }
}