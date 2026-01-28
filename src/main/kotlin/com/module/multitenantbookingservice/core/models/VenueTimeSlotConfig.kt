package com.module.multitenantbookingservice.core.models

import java.time.LocalTime
import java.util.*

enum class TimeSlotType(val typeName: String) {
    UNATTENDED("UNATTENDED"),    // Off-peak hours
    REGULAR("REGULAR"),          // Regular hours
    PEAK("PEAK")                 // Peak hours
}

class VenueTimeSlotConfig(
    val id: UUID = UUID.randomUUID(),
    val venue: Venue,
    val slotType: TimeSlotType,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val priceMultiplier: Double, // 1.0 = base price, 1.5 = 50% markup, 0.8 = 20% discount
    val additionalFee: Double = 0.0 // Additional fee
) {
    init {
        require(startTime < endTime) { "End time must be greater than start time" }
        require(priceMultiplier >= 0) { "Price multiplier cannot be negative" }
        require(additionalFee >= 0) { "Additional fee cannot be negative" }
    }
}