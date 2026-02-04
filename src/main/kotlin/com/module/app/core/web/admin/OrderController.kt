package com.module.app.core.web.admin

import com.module.app.core.models.Order
import com.module.app.core.models.PaymentStatus
import com.module.app.core.service.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/admin/orders")
class OrderController(
    private val orderService: OrderService
) {
    // Order endpoints
    @PostMapping
    fun createOrder(@RequestBody request: OrderCreation): ResponseEntity<Order> {
        val order = orderService.createOrder(request)
        return ResponseEntity(order, HttpStatus.CREATED)
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: UUID): ResponseEntity<Order> {
        val order = orderService.getOrderById(orderId)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/search")
    fun searchOrders(
        @RequestParam(required = false) identityId: UUID?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) status: PaymentStatus?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sort: Sort.Direction
    ): ResponseEntity<Page<Order>> {
        val query = OrderQuery(
            identityId = identityId,
            email = email,
            paymentStatus = status
        )

        val pageable = PageRequest.of(page, size, Sort.by(sort, sortBy))

        val orders = orderService.searchOrders(query, pageable)
        return ResponseEntity.ok(orders)
    }

    @PutMapping("/{orderId}")
    fun updateOrder(
        @PathVariable orderId: UUID,
        @RequestBody update: OrderUpdate
    ): ResponseEntity<Order> {
        val order = orderService.updateOrder(orderId, update)
        return ResponseEntity.ok(order)
    }

    @PatchMapping("/{orderId}/payment-status")
    fun updatePaymentStatus(
        @PathVariable orderId: UUID,
        @RequestBody update: PaymentStatusUpdate
    ): ResponseEntity<Order> {
        val order = orderService.updatePaymentStatus(orderId, update)
        return ResponseEntity.ok(order)
    }

    @DeleteMapping("/{orderId}")
    fun deleteOrder(@PathVariable orderId: UUID): ResponseEntity<Map<String, String>> {
        orderService.deleteOrder(orderId)
        return ResponseEntity.ok(mapOf("message" to "Order deleted successfully"))
    }
}

