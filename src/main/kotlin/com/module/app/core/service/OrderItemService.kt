package com.module.app.core.service

import com.module.app.core.models.OrderItem
import com.module.app.core.repository.OrderItemRepository
import com.module.app.security.BulkUpdatePartialFailure
import com.module.app.security.OrderAlreadyPaidModificationDenied
import com.module.app.security.OrderItemNotFound
import io.github.common.permission.annotation.Require
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull


data class OrderItemUpdate(
    val description: String? = null,
    val amount: Int? = null
)

data class BulkOrderItemUpdate(
    val itemIds: List<UUID>,
    val update: OrderItemUpdate
)

interface OrderItemService {
    fun getOrderItem(itemId: UUID): OrderItem
    fun getAllOrderItems(): List<OrderItem>
    fun updateOrderItem(itemId: UUID, update: OrderItemUpdate): OrderItem
    fun bulkUpdateOrderItems(bulkUpdate: BulkOrderItemUpdate): List<OrderItem>
    fun deleteOrderItem(itemId: UUID)
    fun getOrderItemsByAmountRange(minAmount: Int, maxAmount: Int): List<OrderItem>
}

@Service
class DefaultOrderItemService(
    private val orderItemRepository: OrderItemRepository
): OrderItemService {
    /**
     * 查詢訂單項目
     */
    @Require("orderitems:read")
    @Transactional(readOnly = true)
    override fun getOrderItem(itemId: UUID): OrderItem {
        return orderItemRepository.findById(itemId).getOrNull() ?: throw OrderItemNotFound
    }

    /**
     * 查詢所有訂單項目
     */
    @Require("orderitems:read")
    @Transactional(readOnly = true)
    override fun getAllOrderItems(): List<OrderItem> {
        return orderItemRepository.findAll()
    }

    /**
     * 更新訂單項目信息
     */
    @Require("orderitems:update")
    @Transactional
    override fun updateOrderItem(itemId: UUID, update: OrderItemUpdate): OrderItem {
        val orderItem = orderItemRepository.findById(itemId).getOrNull() ?: throw OrderItemNotFound

        // 更新描述
        update.description?.let { orderItem.description = it }

        // 更新金額
        update.amount?.let { newAmount ->
            // 檢查訂單是否已付款，如果已付款則不允許修改金額
            if (orderItem.order.isPaid()) {
                throw OrderAlreadyPaidModificationDenied.withDetails("Cannot modify amount of order item in a paid order")
            }
            orderItem.amount = newAmount
        }

        return orderItemRepository.save(orderItem)
    }

    /**
     * 刪除訂單項目
     */
    @Require("orderitems:delete")
    @Transactional
    override fun deleteOrderItem(itemId: UUID) {
        val orderItem = orderItemRepository.findById(itemId).getOrNull() ?: return
        orderItemRepository.delete(orderItem)
    }

    /**
     * 根據金額範圍查詢訂單項目
     */
    @Require("orderitems:read")
    @Transactional(readOnly = true)
    override fun getOrderItemsByAmountRange(minAmount: Int, maxAmount: Int): List<OrderItem> {
        return orderItemRepository.findAll()
            .filter { it.amount in minAmount..maxAmount }
    }

    /**
     * 批量更新訂單項目
     */
    @Require("orderitems:update")
    @Transactional
    override fun bulkUpdateOrderItems(bulkUpdate: BulkOrderItemUpdate): List<OrderItem> {
        val updatedItems = mutableListOf<OrderItem>()

        bulkUpdate.itemIds.forEach { itemId ->
            try {
                val updatedItem = updateOrderItem(itemId, bulkUpdate.update)
                updatedItems.add(updatedItem)
            } catch (e: Exception) {
                // 記錄錯誤但繼續處理其他項目
                throw BulkUpdatePartialFailure.withDetails(
                    "Failed to update item $itemId: ${e.message}"
                )
            }
        }

        return updatedItems
    }
}