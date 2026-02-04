package com.module.app.core.models

import jakarta.persistence.*



enum class BookingSlotType {
    HALF_HOUR,
    ONE_HOUR,
}

@Embeddable
data class VenueScheduleConfig(
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_slot_type", nullable = false, length = 50)
    var bookingSlotType: BookingSlotType = BookingSlotType.HALF_HOUR,

    @Column(name = "is_schedule_active", nullable = false)
    var isActive: Boolean = true
)