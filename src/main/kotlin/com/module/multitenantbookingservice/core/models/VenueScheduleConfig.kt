package com.module.multitenantbookingservice.core.models

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes



enum class BookingSlotType {
    HALF_HOUR,
    ONE_HOUR,
}

@Embeddable
data class VenueScheduleConfig(
    @Column(name = "booking_slot_type")
    var bookingSlotType: BookingSlotType = BookingSlotType.HALF_HOUR,

    @Column(name = "is_schedule_active")
    var isActive: Boolean = true,
)