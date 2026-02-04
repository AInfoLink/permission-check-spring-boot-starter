package com.module.app.core.models

import com.module.app.security.permission.HasResourceOwner
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

enum class ReferenceType(
    val typeName: String
) {
    // 現有的
    VENUE_BOOKING_REQUEST("VenueBookingRequest"),
    MEMBERSHIP_UPGRADE("MembershipUpgrade"),

    // 訂場相關
    BOOKING_ITEM_DETAIL("BookingItemDetail"),           // 具體的訂場詳情
    QUANTITY_BOOKING_REQUEST("QuantityBookingRequest"), // 數量模式訂場
    QUARTERLY_BOOKING("QuarterlyBooking"),              // 季度性訂場（從原系統看到）

    // 錢包相關
    WALLET_RECHARGE("WalletRecharge"),                  // 錢包儲值
    WALLET_TRANSACTION("WalletTransaction"),            // 錢包交易記錄
    WALLET_ADJUSTMENT("WalletAdjustment"),              // 錢包調整（管理員操作）

    // 優惠折扣相關
    DISCOUNT_COUPON("DiscountCoupon"),                  // 折扣券
    PROMOTIONAL_DISCOUNT("PromotionalDiscount"),        // 促銷折扣
    MEMBERSHIP_DISCOUNT("MembershipDiscount"),          // 會員折扣
    BULK_BOOKING_DISCOUNT("BulkBookingDiscount"),       // 大量訂場折扣

    // 商品服務相關
    MERCHANDISE_ITEM("MerchandiseItem"),                // 實體商品
    SERVICE_ADDON("ServiceAddon"),                      // 額外服務（如清潔、設備租借）
    EQUIPMENT_RENTAL("EquipmentRental"),                // 設備租借

    // 退款相關
    REFUND_REQUEST("RefundRequest"),                    // 退款申請
    PARTIAL_REFUND("PartialRefund"),                    // 部分退款

    // 費用調整
    ADMIN_ADJUSTMENT("AdminAdjustment"),                // 管理員調整
    PENALTY_FEE("PenaltyFee"),                         // 違約金
    LATE_CANCELLATION_FEE("LateCancellationFee"),      // 遲取消費用

    // 特殊項目
    GIFT_CARD("GiftCard"),                             // 禮品卡
    LOYALTY_POINTS_REDEMPTION("LoyaltyPointsRedemption"), // 紅利點數兌換
    PACKAGE_DEAL("PackageDeal"),                       // 套餐優惠

    // 稅務相關
    TAX_ADJUSTMENT("TaxAdjustment"),                   // 稅務調整

    // 系統調整
    SYSTEM_CORRECTION("SystemCorrection"),              // 系統修正
}

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "description", nullable = false, length = 500)
    var description: String,

    @Column(name = "amount", nullable = false)
    var amount: Int,

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 100)
    val referenceType: ReferenceType,

    @Column(name = "reference_id", nullable = false)
    val referenceId: UUID


): HasResourceOwner {
    override fun getResourceOwnerId(): String {
        return order.getResourceOwnerId()
    }
}