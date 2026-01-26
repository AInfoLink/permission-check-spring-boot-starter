package com.module.multitenantbookingservice.core.web.controller

import com.module.multitenantbookingservice.core.models.OrderItem
import com.module.multitenantbookingservice.core.service.OrderItemCreation
import com.module.multitenantbookingservice.core.service.OrderItemService
import com.module.multitenantbookingservice.core.service.OrderItemUpdate
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

    @GetMapping("/{itemId}")
    fun getOrderItem(@PathVariable itemId: UUID): ResponseEntity<OrderItem> {
        val orderItem = orderItemService.getOrderItem(itemId)
        return ResponseEntity.ok(orderItem)
    }

    @GetMapping("/by-category/{categoryId}")
    fun getOrderItemsByCategory(@PathVariable categoryId: UUID): ResponseEntity<List<OrderItem>> {
        val orderItems = orderItemService.getOrderItemsByCategory(categoryId)
        return ResponseEntity.ok(orderItems)
    }

    @GetMapping
    fun getAllOrderItems(): ResponseEntity<List<OrderItem>> {
        val orderItems = orderItemService.getAllOrderItems()
        return ResponseEntity.ok(orderItems)
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


    @GetMapping("/by-category-and-amount-range")
    fun getOrderItemsByCategoryAndAmountRange(
        @RequestParam categoryId: UUID,
        @RequestParam minAmount: Int,
        @RequestParam maxAmount: Int
    ): ResponseEntity<List<OrderItem>> {
        val orderItems = orderItemService.getOrderItemsByCategoryAndAmountRange(categoryId, minAmount, maxAmount)
        return ResponseEntity.ok(orderItems)
    }
}
