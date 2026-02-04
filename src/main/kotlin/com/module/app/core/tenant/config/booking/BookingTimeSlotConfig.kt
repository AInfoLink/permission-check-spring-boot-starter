package com.module.app.core.tenant.config.booking

import com.fasterxml.jackson.annotation.JsonIgnore
import com.module.app.commons.annotation.TenantConfig
import com.module.app.commons.contract.HasDefault
import com.module.app.commons.contract.ValidationRequired
import com.module.app.commons.utils.TenantConfigUtils
import com.module.app.core.strategy.TimeRange
import com.module.app.security.TimeSlotOverlap
import java.time.LocalTime

enum class TimeSlotType(val typeName: String) {
    UNATTENDED("UNATTENDED"),    // Off-peak hours
    REGULAR("REGULAR"),          // Regular hours
    PEAK("PEAK")                 // Peak hours
}

enum class TimeSlotDuration {
    FIRST_HALF_HOUR, // 上半小時
    SECOND_HALF_HOUR, // 下半小時
    FULL_HOUR // 全小時
}


class BookingTimeSlot(
    var slotType: TimeSlotType,
    val hour: Int, // 0-23，代表這個小時
    val priceMultiplier: Double, // 1.0 = base price, 1.5 = 50% markup, 0.8 = 20% discount
    val additionalFee: Double = 0.0,  // Additional fee
    var basePrice: Int,
    val duration: TimeSlotDuration = TimeSlotDuration.FULL_HOUR // 時間段
) {

    val isHalfHour: Boolean get() = duration != TimeSlotDuration.FULL_HOUR

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

@TenantConfig("booking.time.slot.config")
class BookingTimeSlotConfig(
    val isConfigured : Boolean = false,
    val timeSlots: MutableSet<BookingTimeSlot> = mutableSetOf()
): ValidationRequired, HasDefault<BookingTimeSlotConfig> {
    companion object {
        val CONFIG_KEY = TenantConfigUtils.getConfigKey(BookingTimeSlotConfig::class)

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




    fun querySlot(hour: Int): BookingTimeSlot {
        return timeSlots.find { it.hour == hour } ?: throw IllegalArgumentException("No time slot found for hour $hour")
    }

    override fun validate(): MutableSet<Exception> {
        return mutableSetOf()
    }

    override fun withDefault(): BookingTimeSlotConfig {
        val config = BookingTimeSlotConfig()
        for (hour in 0..23) {
            val slot = BookingTimeSlot(
                slotType = TimeSlotType.REGULAR,
                hour = hour,
                priceMultiplier = 1.0,
                basePrice = 0
            )
            config.addTimeSlot(slot)
        }
        return config
    }
}