package com.module.multitenantbookingservice.core.models

import jakarta.persistence.*
import java.time.LocalTime
import java.util.*

enum class TimeSlotType(val typeName: String) {
    UNATTENDED("UNATTENDED"),    // Off-peak hours
    REGULAR("REGULAR"),          // Regular hours
    PEAK("PEAK")                 // Peak hours
}

@Entity
@Table(
    name = "venue_time_slot_configs",
    indexes = [
        Index(name = "idx_venue_time", columnList = "venue_id, start_time, end_time")
    ]
)
@EntityListeners(VenueTimeSlotConfigValidator::class)
class VenueTimeSlotConfig(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    val venue: Venue,

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false, length = 50)
    val slotType: TimeSlotType,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "price_multiplier", nullable = false)
    val priceMultiplier: Double, // 1.0 = base price, 1.5 = 50% markup, 0.8 = 20% discount

    @Column(name = "additional_fee", nullable = false)
    val additionalFee: Double = 0.0 // Additional fee
) {
    init {
        require(startTime < endTime) { "End time must be greater than start time" }
        require(priceMultiplier >= 0) { "Price multiplier cannot be negative" }
        require(additionalFee >= 0) { "Additional fee cannot be negative" }
    }

    fun isTimeOverlapWith(other: VenueTimeSlotConfig): Boolean {
        return this.startTime < other.endTime && this.endTime > other.startTime
    }
}