package com.module.multitenantbookingservice.core.tenant.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.module.multitenantbookingservice.commons.ValidationRequired
import com.module.multitenantbookingservice.core.strategy.TimeRange
import com.module.multitenantbookingservice.security.TimeSlotOverlap
import java.time.LocalTime

enum class TimeSlotType(val typeName: String) {
    UNATTENDED("UNATTENDED"),    // Off-peak hours
    REGULAR("REGULAR"),          // Regular hours
    PEAK("PEAK")                 // Peak hours
}

enum class TimeSlotInterval(val seconds: Int) {
    HOURLY(3600),
    HALF_HOURLY(1800),
}

class BookingTimeSlot(
    var slotType: TimeSlotType,
    val hour: Int, // 0-23，代表這個小時
    val priceMultiplier: Double, // 1.0 = base price, 1.5 = 50% markup, 0.8 = 20% discount
    val additionalFee: Double = 0.0,  // Additional fee
    var basePrice: Int,
    var isHalfHour: Boolean = false
) {
    init {
        require(hour in 0..23) { "Hour must be between 0-23" }
        require(priceMultiplier >= 0) { "Price multiplier cannot be negative" }
        require(additionalFee >= 0) { "Additional fee cannot be negative" }
    }

    fun asTimeRange(): TimeRange {
        val startTime = LocalTime.of(hour, 0)
        val endTime = LocalTime.of((hour + 1) % 24, 0)
        return TimeRange(startTime, endTime)
    }

    @JsonIgnore
    fun getHourCode(): Int = hour

    fun getPriceForSlot(): Double {
        val base = if (isHalfHour)
            basePrice / 2.0 else
            basePrice.toDouble()
        return base * priceMultiplier + additionalFee
    }
}

class BookingTimeSlotConfig(
    val isConfigured : Boolean = false,
    val timeSlots: MutableSet<BookingTimeSlot> = mutableSetOf()
): ValidationRequired {
    companion object {
        val CONFIG_KEY = "booking.time.slot.config"
    }
    fun addTimeSlot(timeSlot: BookingTimeSlot) {
        // Check for hour conflicts
        timeSlots.forEach { slot ->
            if (slot.hour == timeSlot.hour) {
                throw TimeSlotOverlap.withDetails(
                    "New time slot [hour ${timeSlot.hour}] conflicts with existing slot [hour ${slot.hour}]"
                )
            }
        }
        timeSlots.add(timeSlot)
    }

    fun withDefault(interval: TimeSlotInterval): BookingTimeSlotConfig {
        for (hour in 0..23) {
            val slot = BookingTimeSlot(
                slotType = TimeSlotType.REGULAR,
                hour = hour,
                priceMultiplier = 1.0,
                basePrice = 0
            )
            addTimeSlot(slot)
        }
        return this
    }


    fun querySlot(hour: Int): BookingTimeSlot {
        return timeSlots.find { it.hour == hour } ?: throw IllegalArgumentException("No time slot found for hour $hour")
    }

    override fun validate(): MutableSet<Exception> {
        return mutableSetOf()
    }

}