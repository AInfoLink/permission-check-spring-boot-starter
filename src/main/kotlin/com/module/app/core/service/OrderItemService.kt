package com.module.app.core.service

import com.module.app.core.models.OrderItem
import com.module.app.core.models.OrderItemCategory
import com.module.app.core.repository.OrderItemCategoryRepository
import com.module.app.core.repository.OrderItemRepository
import com.module.app.security.*
import com.module.app.security.annotation.Permission
import com.module.app.security.annotation.Require
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull


data class OrderItemUpdate(
    val description: String? = null,
    val amount: Int? = null,
    val categoryId: UUID? = null
)

data class BulkOrderItemUpdate(
    val itemIds: List<UUID>,
    val update: OrderItemUpdate
)

interface OrderItemService {
    fun validateAmountForCategory(category: OrderItemCategory, amount: Int)
    fun getOrderItem(itemId: UUID): OrderItem
    fun getOrderItemsByCategory(categoryId: UUID): List<OrderItem>
    fun getOrderItemsByCategory(category: OrderItemCategory): List<OrderItem>
    fun getAllOrderItems(): List<OrderItem>
    fun updateOrderItem(itemId: UUID, update: OrderItemUpdate): OrderItem
    fun bulkUpdateOrderItems(bulkUpdate: BulkOrderItemUpdate): List<OrderItem>
    fun deleteOrderItem(itemId: UUID)
    fun validateOrderItemAmount(categoryId: UUID, amount: Int): Boolean
    fun getOrderItemsByCategoryAndAmountRange(categoryId: UUID, minAmount: Int, maxAmount: Int): List<OrderItem>
}

@Service
class DefaultOrderItemService(
    private val orderItemRepository: OrderItemRepository,
    private val orderItemCategoryRepository: OrderItemCategoryRepository
): OrderItemService {
    /**
     * 查詢訂單項目
     */
    @Require(Permission.ORDER_ITEMS_READ)
    @Transactional(readOnly = true)
    override fun getOrderItem(itemId: UUID): OrderItem {
        return orderItemRepository.findById(itemId).getOrNull() ?: throw OrderItemNotFound
    }

    /**
     * 根據分類 ID 查詢訂單項目
     */
    @Require(Permission.ORDER_ITEMS_READ)
    @Transactional(readOnly = true)
    override fun getOrderItemsByCategory(categoryId: UUID): List<OrderItem> {
        return orderItemRepository.findByCategoryId(categoryId)
    }

    /**
     * 根據分類實體查詢訂單項目
     */
    @Require(Permission.ORDER_ITEMS_READ)
    @Transactional(readOnly = true)
    override fun getOrderItemsByCategory(category: OrderItemCategory): List<OrderItem> {
        return orderItemRepository.findByCategory(category)
    }

    /**
     * 查詢所有訂單項目
     */
    @Require(Permission.ORDER_ITEMS_READ)
    @Transactional(readOnly = true)
    override fun getAllOrderItems(): List<OrderItem> {
        return orderItemRepository.findAll()
    }

    /**
     * 更新訂單項目信息
     */
    @Require(Permission.ORDER_ITEMS_UPDATE)
    @Transactional
    override fun updateOrderItem(itemId: UUID, update: OrderItemUpdate): OrderItem {
        val orderItem = orderItemRepository.findById(itemId).getOrNull() ?: throw OrderItemNotFound

        // 更新描述
        update.description?.let { orderItem.description = it }

        // 更新金額
        update.amount?.let { newAmount ->
            orderItem.amount = newAmount
        }

        // 更新分類
        update.categoryId?.let { newCategoryId ->
            val newCategory = orderItemCategoryRepository.findById(newCategoryId).getOrNull()
                ?: throw ItemCategoryNotFound
            orderItem.category = newCategory
        }

        // 如果有金額更新，驗證新金額是否符合分類規則
        if (update.amount != null) {
            if (orderItem.order.isPaid()) {
                throw OrderAlreadyPaidModificationDenied.withDetails("Cannot modify amount of order item in a paid order")
            }
            validateAmountForCategory(orderItem.category, orderItem.amount)
        }

        return orderItemRepository.save(orderItem)
    }

    /**
     * 刪除訂單項目
     */
    @Require(Permission.ORDER_ITEMS_DELETE)
    @Transactional
    override fun deleteOrderItem(itemId: UUID) {
        val orderItem = orderItemRepository.findById(itemId).getOrNull() ?: return
        orderItemRepository.delete(orderItem)
    }

    /**
     * 驗證訂單項目金額是否符合分類規則
     */
    @Require(Permission.ORDER_ITEMS_READ)
    override fun validateOrderItemAmount(categoryId: UUID, amount: Int): Boolean {
        val category = orderItemCategoryRepository.findById(categoryId).getOrNull()
            ?: throw ItemCategoryNotFound
        return category.validateAmountForOperationType(amount)
    }

    override fun validateAmountForCategory(category: OrderItemCategory, amount: Int) {
        if (!category.validateAmountForOperationType(amount)) {
            throw InvalidAmountForCategory.withDetails(
                "Amount $amount is invalid for category ${category.code} with operation type ${category.operationType}"
            )
        }
    }

    /**
     * 根據分類和金額範圍查詢訂單項目
     */
    @Require(Permission.ORDER_ITEMS_READ)
    @Transactional(readOnly = true)
    override fun getOrderItemsByCategoryAndAmountRange(categoryId: UUID, minAmount: Int, maxAmount: Int): List<OrderItem> {
        return orderItemRepository.findByCategoryId(categoryId)
            .filter { it.amount in minAmount..maxAmount }
    }
    /**
     * 批量更新訂單項目
     */
    @Require(Permission.ORDER_ITEMS_UPDATE)
    @Transactional
    override fun bulkUpdateOrderItems(bulkUpdate: BulkOrderItemUpdate): List<OrderItem> {
        val updatedItems = mutableListOf<OrderItem>()

        bulkUpdate.itemIds.forEach { itemId ->
            try {
                val updatedItem = updateOrderItem(itemId, bulkUpdate.update)
                updatedItems.add(updatedItem)
            } catch (e: Exception) {
                // 記錄錯誤但繼續處理其他項目
                // 在實際應用中可能需要更細緻的錯誤處理策略
                throw BulkUpdatePartialFailure.withDetails(
                    "Failed to update item $itemId: ${e.message}"
                )
            }
        }

        return updatedItems
    }
}