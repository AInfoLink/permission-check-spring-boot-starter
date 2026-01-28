package com.module.multitenantbookingservice.core.models

import com.module.multitenantbookingservice.security.permission.HasResourceOwner
import com.module.multitenantbookingservice.security.model.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

enum class IdentityType(
    val typeName: String
) {
    USER("USER"),
    GUEST("GUEST"),
    SYSTEM("SYSTEM")
}

enum class PaymentStatus(
    val statusName: String
) {
    PENDING("PENDING"),
    PAID("PAID"),
    FAILED("FAILED")
}


@Entity
@Table(name = "order_identities")
class OrderIdentity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    val type: IdentityType,

    @Column(name = "email", nullable = false, length = 255)
    val email: String,

    @Column(name = "name", nullable = false, length = 255)
    val name: String,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    val user: User? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant
) : HasResourceOwner {
    override fun getResourceOwnerId(): String {
        return when (type) {
            IdentityType.USER -> user?.id?.toString()
                ?: throw IllegalStateException("OrderIdentity of type USER must be associated with a User")
            IdentityType.GUEST -> email
            IdentityType.SYSTEM -> "SYSTEM"
        }
    }
}

@Entity
@Table(name = "orders")
class Order(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "description", nullable = false, length = 1000)
    var description: String,

    @Column(name = "amount", nullable = false)
    var amount: Int,

    @ManyToOne
    @JoinColumn(name = "order_identity_id", nullable = false)
    val identity: OrderIdentity,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableSet<OrderItem> = mutableSetOf()
): HasResourceOwner {
    val email: String get() = identity.email

    fun isPaid(): Boolean = this.paymentStatus == PaymentStatus.PAID

    override fun getResourceOwnerId(): String {
        return identity.getResourceOwnerId()
    }
}
