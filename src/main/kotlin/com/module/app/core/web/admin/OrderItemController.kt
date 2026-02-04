package com.module.app.core.web.admin

import com.module.app.core.models.OrderItem
import com.module.app.core.service.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/admin/order-items")
class OrderItemController(
    private val orderItemService: OrderItemService
) {
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

    @PutMapping("/bulk-update")
    fun bulkUpdateOrderItems(
        @RequestBody bulkUpdate: BulkOrderItemUpdate
    ): ResponseEntity<List<OrderItem>> {
        val updatedItems = orderItemService.bulkUpdateOrderItems(bulkUpdate)
        return ResponseEntity.ok(updatedItems)
    }
}
