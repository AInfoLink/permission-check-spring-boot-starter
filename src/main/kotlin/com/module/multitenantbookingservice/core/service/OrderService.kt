package com.module.multitenantbookingservice.core.service

import com.module.multitenantbookingservice.core.models.*
import com.module.multitenantbookingservice.core.repository.OrderIdentityRepository
import com.module.multitenantbookingservice.core.repository.OrderRepository
import com.module.multitenantbookingservice.core.repository.OrderItemCategoryRepository
import com.module.multitenantbookingservice.security.OrderIdentityAlreadyExists
import com.module.multitenantbookingservice.security.OrderIdentityNotFound
import com.module.multitenantbookingservice.security.OrderNotFound
import com.module.multitenantbookingservice.security.UserNotFound
import com.module.multitenantbookingservice.security.ItemCategoryNotFound
import com.module.multitenantbookingservice.security.InvalidAmountForCategory
import com.module.multitenantbookingservice.security.annotation.Permission
import com.module.multitenantbookingservice.security.annotation.Require
import com.module.multitenantbookingservice.security.repository.UserRepository
import com.module.multitenantbookingservice.security.repository.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

data class OrderItemCreation(
    val description: String,
    val amount: Int,
    val categoryId: UUID
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
    private val userRepository: UserRepository,
    private val orderItemCategoryRepository: OrderItemCategoryRepository
): OrderService {

    /**
     * 創建訂單身份，可選擇關聯已存在的用戶
     */
    @Require(Permission.ORDERS_CREATE)
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
    @Require(Permission.ORDERS_CREATE)
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

        // 建立訂單項目
        order.items.forEach { itemData ->
            val category = orderItemCategoryRepository.findById(itemData.categoryId).getOrNull()
                ?: throw ItemCategoryNotFound

            // 驗證金額是否符合分類的操作類型
            validateAmountForCategory(category, itemData.amount)

            val orderItem = OrderItem(
                description = itemData.description,
                amount = itemData.amount,
                category = category,
                order = newOrder,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            newOrder.items.add(orderItem)
        }

        return orderRepository.save(newOrder)  // Cascade 會自動儲存 items
    }

    /**
     * 查詢訂單
     */
    @Require(Permission.ORDERS_READ)
    @Transactional(readOnly = true)
    override fun getOrderById(orderId: UUID): Order {
        return orderRepository.findById(orderId).getOrNull() ?: throw OrderNotFound
    }

    /**
     * 統一的訂單搜尋方法，支援多種查詢條件和分頁
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
     * 查詢所有訂單
     */
    @Require(Permission.ORDERS_READ)
    @Transactional(readOnly = true)
    override fun getAllOrders(): List<Order> {
        return orderRepository.findAll()
    }

    /**
     * 更新訂單信息
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
     * 更新訂單支付狀態
     */
    @Require(Permission.ORDERS_UPDATE)
    @Transactional
    override fun updatePaymentStatus(orderId: UUID, update: PaymentStatusUpdate): Order {
        val order = orderRepository.findById(orderId).getOrNull() ?: throw OrderNotFound
        order.paymentStatus = update.status
        return orderRepository.save(order)
    }

    /**
     * 刪除訂單
     */
    @Require(Permission.ORDERS_DELETE)
    @Transactional
    override fun deleteOrder(orderId: UUID) {
        val order = orderRepository.findById(orderId).getOrNull() ?: return
        orderRepository.delete(order)
    }


    fun validateAmountForCategory(category: OrderItemCategory, amount: Int) {
        if (!category.validateAmountForOperationType(amount)) {
            throw InvalidAmountForCategory.withDetails(
                "Amount $amount is invalid for category ${category.code} with operation type ${category.operationType}"
            )
        }
    }
}