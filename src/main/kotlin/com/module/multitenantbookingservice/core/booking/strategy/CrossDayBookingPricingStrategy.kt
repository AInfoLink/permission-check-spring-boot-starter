package com.module.multitenantbookingservice.core.booking.strategy

import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CrossDayBookingPricingStrategy : PricingStrategy {

    override var priority: Int = 0 // Highest priority, handle cross-day first

    override val name: String
        get() = Strategy.CROSS_DAY_HELPER.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        val newItems = currentResult.items.toMutableSet()

        // Check if booking spans across days
        if (!isCrossDayBooking(context)) {
            // Same day booking, do nothing, let other strategies handle
            return PricingResult(newItems)
        }

        /**
         * Split cross-day time into TimeRange segments using TimeRange.expandOvernight()
         *
         * Example: 2024-01-01 23:00 to 2024-01-02 01:00
         * -> [23:00-24:00 (Day 1), 00:00-01:00 (Day 2)]
         */
        val timeRangeSegments = context.bookingTimeRange.expandOvernight()
        timeRangeSegments.forEachIndexed { index, timeRange ->
            val dayLabel = "Day ${index + 1}"
            newItems.add(
                PricingItemResult(
                    itemName = "Booking Period ($dayLabel)",
                    description = "$dayLabel ${timeRange.startTime.toLocalTime()}-${timeRange.endTime.toLocalTime()} (${formatDuration(timeRange.duration)})",
                    timeRange = timeRange,
                    price = 0.0 // Price to be calculated by other strategies
            ))
        }

        return PricingResult(newItems)
    }

    /**
     * Check if booking spans across days
     *
     * Examples:
     * 23:00-01:00 = cross-day
     * 10:00-12:00 = same day
     */
    private fun isCrossDayBooking(context: PricingContext): Boolean {
        val startTime = context.bookingTimeRange.startTime.toLocalTime()
        val endTime = context.bookingTimeRange.endTime.toLocalTime()

        return startTime >= endTime
    }



    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        return "${hours}h ${minutes}min"
    }

}