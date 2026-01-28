package com.module.multitenantbookingservice.core.booking.strategy

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

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

        // Cross-day booking, split into two time segment items
        val timeSegments = createCrossDayTimeSegments(context)
        timeSegments.forEach { segment ->
            newItems.add(
                PricingItemResult(
                    itemName = "Booking Period (${segment.dayLabel})",
                    description = "${segment.dayLabel} ${segment.startTime}-${segment.endTime} (${formatDuration(segment.duration)})",
                    timeRange = segment.asTimeRange(),
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

    /**
     * Split cross-day time into two time segments
     *
     * Example: 2024-01-01 23:00 to 2024-01-02 01:00
     * -> [23:00-24:00 (Day 1), 00:00-01:00 (Day 2)]
     */
    private fun createCrossDayTimeSegments(context: PricingContext): List<TimeSegment> {
        val bookingStartTime = context.bookingTimeRange.startTime
        val bookingEndTime = context.bookingTimeRange.endTime

        val startTime = bookingStartTime.toLocalTime()
        val endTime = bookingEndTime.toLocalTime()
        val startDate = bookingStartTime.toLocalDate()
        val endDate = bookingEndTime.toLocalDate()

        // First segment: start time to midnight
        val firstSegmentEnd = LocalTime.MAX // 23:59:59.999999999 (close to 24:00)
        val firstSegmentDuration = Duration.between(startTime, firstSegmentEnd.plusNanos(1)) // Add 1 nanosecond to make it full 24:00

        // Second segment: midnight to end time
        val secondSegmentStart = LocalTime.MIN // 00:00
        val secondSegmentDuration = Duration.between(secondSegmentStart, endTime)

        return listOf(
            TimeSegment(
                startTime = startTime,
                endTime = firstSegmentEnd,
                duration = firstSegmentDuration,
                dayLabel = "Day 1",
                date = startDate
            ),
            TimeSegment(
                startTime = secondSegmentStart,
                endTime = endTime,
                duration = secondSegmentDuration,
                dayLabel = "Day 2",
                date = endDate
            )
        )
    }

    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        return "${hours}h ${minutes}min"
    }

    /**
     * Internal class: Time segment information
     */
    private data class TimeSegment(
        val date: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime,
        val duration: Duration,
        val dayLabel: String
    ) {
        fun asTimeRange(): TimeRange {
            return TimeRange(date, startTime, endTime)
        }
    }
}