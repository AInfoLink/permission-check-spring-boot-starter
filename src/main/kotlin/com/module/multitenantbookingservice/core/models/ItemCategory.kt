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

@Entity
@Table(
    name = "item_categories",
    indexes = [
        Index(name = "idx_item_categories_code", columnList = "code", unique = true),
        Index(name = "idx_item_categories_type", columnList = "type")
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

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)