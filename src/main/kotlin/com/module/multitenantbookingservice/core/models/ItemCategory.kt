package com.module.multitenantbookingservice.core.models

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

enum class CategoryType {
    SYSTEM_MANAGED,    // 系統預設，不可刪除
    USER_MANAGED       // 用戶自訂，可編輯刪除
}

enum class OperationType {
    CHARGE,     // 收費操作：正值金額，如預訂、租借
    REFUND,     // 退費操作：負值金額，如取消、退款
    NEUTRAL     // 中性操作：無金額變動，如查詢、確認
}

@Entity
@Table(
    name = "item_categories",
    indexes = [
        Index(name = "idx_item_categories_code", columnList = "code", unique = true),
        Index(name = "idx_item_categories_type", columnList = "type"),
        Index(name = "idx_item_categories_operation_type", columnList = "operation_type")
    ]
)
class ItemCategory(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "code", nullable = false, length = 50, unique = true)
    val code: String,  // BOOKING, RENTAL, etc. 用於程式識別

    @Column(name = "name", nullable = false, length = 100)
    var name: String,  // 顯示名稱，可多語言

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
     * 驗證金額是否符合操作類型
     * @param amount 交易金額
     * @return 如果金額符合操作類型則返回true
     */
    fun validateAmountForOperationType(amount: Double): Boolean {
        return when (operationType) {
            OperationType.CHARGE -> amount > 0    // 收費操作必須為正值
            OperationType.REFUND -> amount < 0    // 退費操作必須為負值
            OperationType.NEUTRAL -> amount == 0.0 // 中性操作金額為零
        }
    }

    /**
     * 檢查是否為收費操作
     */
    fun isChargeOperation(): Boolean = operationType == OperationType.CHARGE

    /**
     * 檢查是否為退費操作
     */
    fun isRefundOperation(): Boolean = operationType == OperationType.REFUND

    /**
     * 檢查是否為中性操作（無金額變動）
     */
    fun isNeutralOperation(): Boolean = operationType == OperationType.NEUTRAL
}