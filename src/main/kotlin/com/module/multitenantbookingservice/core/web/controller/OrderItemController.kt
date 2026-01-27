package com.module.multitenantbookingservice.core.web.controller

import com.module.multitenantbookingservice.core.models.OrderItem
import com.module.multitenantbookingservice.core.service.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/order-items")
class OrderItemController(
    private val orderItemService: OrderItemService
) {

    @PostMapping
    fun createOrderItem(@RequestBody request: OrderItemCreation): ResponseEntity<OrderItem> {
        val orderItem = orderItemService.createOrderItem(request)
        return ResponseEntity(orderItem, HttpStatus.CREATED)
    }

    @PutMapping("/{itemId}")
    fun updateOrderItem(
        @PathVariable itemId: UUID,
        @RequestBody update: OrderItemUpdate
    ): ResponseEntity<OrderItem> {
        val orderItem = orderItemService.updateOrderItem(itemId, update)
        return ResponseEntity.ok(orderItem)
    }

    @DeleteMapping("/{itemId}")
    fun deleteOrderItem(@PathVariable itemId: UUID): ResponseEntity<Map<String, String>> {
        orderItemService.deleteOrderItem(itemId)
        return ResponseEntity.ok(mapOf("message" to "Order item deleted successfully"))
    }

    @PatchMapping("/{itemId}/amount")
    fun updateOrderItemAmount(
        @PathVariable itemId: UUID,
        @RequestBody amountUpdate: OrderItemAmountUpdate
    ): ResponseEntity<OrderItem> {
        val orderItem = orderItemService.updateOrderItemAmount(itemId, amountUpdate)
        return ResponseEntity.ok(orderItem)
    }

    @PatchMapping("/{itemId}/category")
    fun updateOrderItemCategory(
        @PathVariable itemId: UUID,
        @RequestBody categoryUpdate: OrderItemCategoryUpdate
    ): ResponseEntity<OrderItem> {
        val orderItem = orderItemService.updateOrderItemCategory(itemId, categoryUpdate)
        return ResponseEntity.ok(orderItem)
    }

    @PutMapping("/bulk-update")
    fun bulkUpdateOrderItems(
        @RequestBody bulkUpdate: BulkOrderItemUpdate
    ): ResponseEntity<List<OrderItem>> {
        val updatedItems = orderItemService.bulkUpdateOrderItems(bulkUpdate)
        return ResponseEntity.ok(updatedItems)
    }
}
