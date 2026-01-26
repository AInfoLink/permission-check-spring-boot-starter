package com.module.multitenantbookingservice.core.service

import com.module.multitenantbookingservice.core.models.ItemCategory
import com.module.multitenantbookingservice.core.models.OrderItem
import com.module.multitenantbookingservice.core.repository.ItemCategoryRepository
import com.module.multitenantbookingservice.core.repository.OrderItemRepository
import com.module.multitenantbookingservice.security.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class OrderItemCreation(
    val description: String,
    val amount: Int,
    val categoryId: UUID
)

data class OrderItemUpdate(
    val description: String? = null,
    val amount: Int? = null,
    val categoryId: UUID? = null
)

data class OrderItemAmountUpdate(
    val amount: Int
)

data class OrderItemCategoryUpdate(
    val categoryId: UUID
)

data class BulkOrderItemUpdate(
    val itemIds: List<UUID>,
    val update: OrderItemUpdate
)

interface OrderItemService {
    fun createOrderItem(item: OrderItemCreation): OrderItem
    fun getOrderItem(itemId: UUID): OrderItem
    fun getOrderItemsByCategory(categoryId: UUID): List<OrderItem>
    fun getOrderItemsByCategory(category: ItemCategory): List<OrderItem>
    fun getAllOrderItems(): List<OrderItem>
    fun updateOrderItem(itemId: UUID, update: OrderItemUpdate): OrderItem
    fun updateOrderItemAmount(itemId: UUID, amountUpdate: OrderItemAmountUpdate): OrderItem
    fun updateOrderItemCategory(itemId: UUID, categoryUpdate: OrderItemCategoryUpdate): OrderItem
    fun bulkUpdateOrderItems(bulkUpdate: BulkOrderItemUpdate): List<OrderItem>
    fun deleteOrderItem(itemId: UUID)
    fun validateOrderItemAmount(categoryId: UUID, amount: Int): Boolean
    fun getOrderItemsByCategoryAndAmountRange(categoryId: UUID, minAmount: Int, maxAmount: Int): List<OrderItem>
}

@Service
class DefaultOrderItemService(
    private val orderItemRepository: OrderItemRepository,
    private val itemCategoryRepository: ItemCategoryRepository
): OrderItemService {

    /**
     * 創建訂單項目
     */
    @Transactional
    override fun createOrderItem(item: OrderItemCreation): OrderItem {
        val category = itemCategoryRepository.findById(item.categoryId).getOrNull()
            ?: throw ItemCategoryNotFound

        // 驗證金額是否符合分類的操作類型
        if (!category.validateAmountForOperationType(item.amount.toDouble())) {
            throw InvalidAmountForCategory.withDetails(
                "Amount ${item.amount} is invalid for category ${category.code} with operation type ${category.operationType}"
            )
        }

        val orderItem = OrderItem(
            description = item.description,
            amount = item.amount,
            category = category,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        return orderItemRepository.save(orderItem)
    }

    /**
     * 查詢訂單項目
     */
    @Transactional(readOnly = true)
    override fun getOrderItem(itemId: UUID): OrderItem {
        return orderItemRepository.findById(itemId).getOrNull() ?: throw OrderItemNotFound
    }

    /**
     * 根據分類 ID 查詢訂單項目
     */
    @Transactional(readOnly = true)
    override fun getOrderItemsByCategory(categoryId: UUID): List<OrderItem> {
        return orderItemRepository.findByCategoryId(categoryId)
    }

    /**
     * 根據分類實體查詢訂單項目
     */
    @Transactional(readOnly = true)
    override fun getOrderItemsByCategory(category: ItemCategory): List<OrderItem> {
        return orderItemRepository.findByCategory(category)
    }

    /**
     * 查詢所有訂單項目
     */
    @Transactional(readOnly = true)
    override fun getAllOrderItems(): List<OrderItem> {
        return orderItemRepository.findAll()
    }

    /**
     * 更新訂單項目信息
     */
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
            val newCategory = itemCategoryRepository.findById(newCategoryId).getOrNull()
                ?: throw ItemCategoryNotFound
            orderItem.category = newCategory
        }

        // 如果有金額更新，驗證新金額是否符合分類規則
        if (update.amount != null) {
            if (!orderItem.category.validateAmountForOperationType(orderItem.amount.toDouble())) {
                throw InvalidAmountForCategory.withDetails(
                    "Amount ${orderItem.amount} is invalid for category ${orderItem.category.code} with operation type ${orderItem.category.operationType}"
                )
            }
        }

        return orderItemRepository.save(orderItem)
    }

    /**
     * 刪除訂單項目
     */
    @Transactional
    override fun deleteOrderItem(itemId: UUID) {
        val orderItem = orderItemRepository.findById(itemId).getOrNull() ?: return
        orderItemRepository.delete(orderItem)
    }

    /**
     * 驗證訂單項目金額是否符合分類規則
     */
    override fun validateOrderItemAmount(categoryId: UUID, amount: Int): Boolean {
        val category = itemCategoryRepository.findById(categoryId).getOrNull()
            ?: throw ItemCategoryNotFound
        return category.validateAmountForOperationType(amount.toDouble())
    }

    /**
     * 根據分類和金額範圍查詢訂單項目
     */
    @Transactional(readOnly = true)
    override fun getOrderItemsByCategoryAndAmountRange(categoryId: UUID, minAmount: Int, maxAmount: Int): List<OrderItem> {
        return orderItemRepository.findByCategoryId(categoryId)
            .filter { it.amount in minAmount..maxAmount }
    }

    /**
     * 更新訂單項目金額
     */
    @Transactional
    override fun updateOrderItemAmount(itemId: UUID, amountUpdate: OrderItemAmountUpdate): OrderItem {
        val orderItem = orderItemRepository.findById(itemId).getOrNull() ?: throw OrderItemNotFound

        // 驗證新金額是否符合分類規則
        if (!orderItem.category.validateAmountForOperationType(amountUpdate.amount.toDouble())) {
            throw InvalidAmountForCategory.withDetails(
                "Amount ${amountUpdate.amount} is invalid for category ${orderItem.category.code} with operation type ${orderItem.category.operationType}"
            )
        }

        orderItem.amount = amountUpdate.amount
        return orderItemRepository.save(orderItem)
    }

    /**
     * 更新訂單項目分類
     */
    @Transactional
    override fun updateOrderItemCategory(itemId: UUID, categoryUpdate: OrderItemCategoryUpdate): OrderItem {
        val orderItem = orderItemRepository.findById(itemId).getOrNull() ?: throw OrderItemNotFound
        val newCategory = itemCategoryRepository.findById(categoryUpdate.categoryId).getOrNull()
            ?: throw ItemCategoryNotFound

        // 驗證當前金額是否符合新分類的規則
        if (!newCategory.validateAmountForOperationType(orderItem.amount.toDouble())) {
            throw InvalidAmountForCategory.withDetails(
                "Amount ${orderItem.amount} is invalid for category ${newCategory.code} with operation type ${newCategory.operationType}"
            )
        }

        orderItem.category = newCategory
        return orderItemRepository.save(orderItem)
    }

    /**
     * 批量更新訂單項目
     */
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