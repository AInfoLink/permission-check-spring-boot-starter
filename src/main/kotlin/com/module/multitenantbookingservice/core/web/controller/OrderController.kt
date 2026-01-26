package com.module.multitenantbookingservice.core.web.controller

import com.module.multitenantbookingservice.core.models.IdentityType
import com.module.multitenantbookingservice.core.models.Order
import com.module.multitenantbookingservice.core.models.PaymentStatus
import com.module.multitenantbookingservice.core.service.OrderCreation
import com.module.multitenantbookingservice.core.service.OrderService
import com.module.multitenantbookingservice.core.service.OrderUpdate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/orders")
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

    @GetMapping("/by-identity/{identityId}")
    fun getOrdersByIdentity(@PathVariable identityId: UUID): ResponseEntity<List<Order>> {
        val orders = orderService.getOrdersByIdentity(identityId)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/by-email/{email}")
    fun getOrdersByEmail(@PathVariable email: String): ResponseEntity<List<Order>> {
        val orders = orderService.getOrdersByEmail(email)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/by-status/{status}")
    fun getOrdersByPaymentStatus(@PathVariable status: PaymentStatus): ResponseEntity<List<Order>> {
        val orders = orderService.getOrdersByPaymentStatus(status)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/by-email-and-status")
    fun getOrdersByEmailAndStatus(
        @RequestParam email: String,
        @RequestParam status: PaymentStatus
    ): ResponseEntity<List<Order>> {
        val orders = orderService.getOrdersByEmailAndStatus(email, status)
        return ResponseEntity.ok(orders)
    }

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<Order>> {
        val orders = orderService.getAllOrders()
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
        @RequestBody request: PaymentStatusUpdateRequest
    ): ResponseEntity<Order> {
        val order = orderService.updatePaymentStatus(orderId, request.status)
        return ResponseEntity.ok(order)
    }

    @DeleteMapping("/{orderId}")
    fun deleteOrder(@PathVariable orderId: UUID): ResponseEntity<Map<String, String>> {
        orderService.deleteOrder(orderId)
        return ResponseEntity.ok(mapOf("message" to "Order deleted successfully"))
    }
}

data class PaymentStatusUpdateRequest(
    val status: PaymentStatus
)

data class FindOrCreateIdentityRequest(
    val email: String,
    val name: String,
    val type: IdentityType,
    val userId: UUID? = null
)