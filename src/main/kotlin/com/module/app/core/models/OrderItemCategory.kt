package com.module.app.core.models

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

enum class CategoryType {
    SYSTEM_MANAGED,    // System default, cannot be deleted
    USER_MANAGED       // User-defined, can be edited and deleted
}

enum class OperationType {
    CHARGE,     // Charge operation: positive amount, e.g., booking, rental
    REFUND,     // Refund operation: negative amount, e.g., cancellation, refund
    NEUTRAL     // Neutral operation: no amount change, e.g., query, confirmation
}

@Entity
@Table(
    name = "order_item_categories",
    indexes = [
        Index(name = "idx_item_categories_code", columnList = "code", unique = true),
        Index(name = "idx_item_categories_type", columnList = "type"),
        Index(name = "idx_item_categories_operation_type", columnList = "operation_type")
    ]
)
class OrderItemCategory(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "code", nullable = false, length = 50, unique = true)
    val code: String,  // BOOKING, RENTAL, etc. Used for program identification

    @Column(name = "name", nullable = false, length = 100)
    var name: String,  // Display name, supports multiple languages

    @Column(name = "description", nullable = true, length = 500)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    val type: CategoryType,

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    val operationType: OperationType,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
) {
    /**
     * Validates whether the amount matches the operation type
     * @param amount Transaction amount
     * @return Returns true if the amount matches the operation type
     */
    fun validateAmountForOperationType(amount: Int): Boolean {
        return when (operationType) {
            OperationType.CHARGE -> amount > 0    // Charge operations must be positive
            OperationType.REFUND -> amount < 0    // Refund operations must be negative
            OperationType.NEUTRAL -> amount == 0 // Neutral operations have zero amount
        }
    }

    /**
     * Checks if this is a charge operation
     */
    fun isChargeOperation(): Boolean = operationType == OperationType.CHARGE

    /**
     * Checks if this is a refund operation
     */
    fun isRefundOperation(): Boolean = operationType == OperationType.REFUND

    /**
     * Checks if this is a neutral operation (no amount change)
     */
    fun isNeutralOperation(): Boolean = operationType == OperationType.NEUTRAL
}