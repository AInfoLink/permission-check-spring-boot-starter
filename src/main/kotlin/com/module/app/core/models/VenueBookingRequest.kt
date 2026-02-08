package com.module.app.core.models

import com.module.app.security.permission.HasResourceOwner
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID


enum class TimeSlotDuration {
    FIRST_HALF_HOUR, // 上半小時
    SECOND_HALF_HOUR, // 下半小時
    FULL_HOUR // 全小時
}

enum class BookingStatus(
    val statusName: String
) {
    PENDING("PENDING"),
    CONFIRMED("CONFIRMED"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED")
}


@Entity
@Table(name = "venue_booking_requests",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["venue_id", "date", "hour", "duration"])
    ]
)
class VenueBookingRequest(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    val venue: Venue,

    @Column(name = "date", nullable = false)
    val date: LocalDate,

    @Column(name = "hour", nullable = false)
    val hour: Int, // 0-23，代表這個小時

    @Enumerated(EnumType.STRING)
    @Column(name = "duration", nullable = false, length = 50)
    val duration: TimeSlotDuration,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: BookingStatus = BookingStatus.PENDING,


    @OneToOne
    @JoinColumn(name = "order_item_id")
    var orderItem: OrderItem,

    @Column(name = "notes", nullable = true, length = 1000)
    var notes: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant


) : HasResourceOwner {
    override fun getResourceOwnerId(): String {
        return orderItem.order.getResourceOwnerId()
    }

    init {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
    }
}