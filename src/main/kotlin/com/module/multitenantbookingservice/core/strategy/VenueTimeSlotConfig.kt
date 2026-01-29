package com.module.multitenantbookingservice.core.strategy

import com.module.multitenantbookingservice.security.TimeSlotOverlap
import java.time.LocalTime
import java.util.*

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
    val id: UUID = UUID.randomUUID(),
    var slotType: TimeSlotType,
    val startTime: LocalTime,
    var endTime: LocalTime,
    val priceMultiplier: Double, // 1.0 = base price, 1.5 = 50% markup, 0.8 = 20% discount
    val additionalFee: Double = 0.0 // Additional fee
) {
    init {
        // This assumes same day time slots; overnight slots will be thrown as invalid
        require(startTime < endTime) { "End time must be greater than start time" }
        require(priceMultiplier >= 0) { "Price multiplier cannot be negative" }
        require(additionalFee >= 0) { "Additional fee cannot be negative" }
    }

    fun asTimeRange(): TimeRange {
        return TimeRange(startTime, endTime)
    }
}

class BookingTimeSlotConfig(
    val id: UUID = UUID.randomUUID(),
    val isConfigured : Boolean = false,
    private val timeSlots: MutableSet<BookingTimeSlot> = mutableSetOf()
) {
    companion object {
        val CONFIG_KEY = "booking.time.slot.config"
    }
    fun addTimeSlot(timeSlot: BookingTimeSlot) {
        val timeSlotRange = timeSlot.asTimeRange()
        timeSlots.forEach { slot ->
            val slotTimeRange = slot.asTimeRange()
            val isOverlap = slotTimeRange.isOverlapWithAllowOvernight(timeSlotRange)
            if (isOverlap) {
                throw TimeSlotOverlap.withDetails(
                    "New time slot [${timeSlot.startTime} - ${timeSlot.endTime}] overlaps with existing slot [${slot.startTime} - ${slot.endTime}]"
                )
            }
        }
        timeSlots.add(timeSlot)
    }

    fun withDefaultConfig(interval: TimeSlotInterval): BookingTimeSlotConfig {
        var currentTime = LocalTime.MIN
        while (currentTime < LocalTime.MAX) {
            val endTime = if (currentTime.hour == 23) LocalTime.MAX else currentTime.plusSeconds(interval.seconds.toLong())
            val slot = BookingTimeSlot(
                slotType = TimeSlotType.REGULAR,
                startTime = currentTime,
                endTime = endTime,
                priceMultiplier = 1.0
            )
            addTimeSlot(slot)
            currentTime = endTime
        }
        return this
    }
}