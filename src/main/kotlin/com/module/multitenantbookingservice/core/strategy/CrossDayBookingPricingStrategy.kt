//package com.module.multitenantbookingservice.core.strategy
//
//import org.springframework.stereotype.Component
//import java.time.Duration
//import java.time.LocalTime
//
//@Component
//class CrossDayBookingPricingStrategy : PricingStrategy {
//
//    override var priority: Int = 0 // Highest priority, handle cross-day first
//
//    override val name: String
//        get() = Strategy.CROSS_DAY_HELPER.strategyName
//
//    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
//        val newItems = currentResult.items.toMutableSet()
//
//        // Check if booking spans across days
//        if (!isCrossDayBooking(context)) {
//            // Same day booking, do nothing, let other strategies handle
//            return PricingResult(newItems)
//        }
//
//        /**
//         * Split cross-day time into TimeRange segments using TimeRange.expandOvernight()
//         *
//         * Example: 2024-01-01 23:00 to 2024-01-02 01:00
//         * -> [23:00-24:00 (Day 1), 00:00-01:00 (Day 2)]
//         */
//        val bookingTimeRange = createTimeRangeFromSlots(context)
//        val timeRangeSegments = bookingTimeRange.expandOvernight()
//        timeRangeSegments.forEachIndexed { index: Int, timeRange: TimeRange ->
//            val dayLabel = "Day ${index + 1}"
//            newItems.add(
//                PricingItemResult(
//                    itemName = "Booking Period ($dayLabel)",
//                    description = "$dayLabel ${timeRange.startTime.toLocalTime()}-${timeRange.endTime.toLocalTime()} (${formatDuration(timeRange.duration)})",
//                    timeRange = timeRange,
//                    price = 0.0 // Price to be calculated by other strategies
//            )
//            )
//        }
//
//        return PricingResult(newItems)
//    }
//
//    /**
//     * Convert booking time slots to a combined time range
//     */
//    private fun createTimeRangeFromSlots(context: PricingContext): TimeRange {
//        val bookingTimeSlots = context.bookingTimeSlots.map { slotView ->
//            context.bookingTimeSlotConfig.querySlot(slotView.hour)
//        }.sortedBy { it.hour }
//
//        val firstSlot = bookingTimeSlots.first()
//        val lastSlot = bookingTimeSlots.last()
//
//        val startTime = LocalTime.of(firstSlot.hour, 0)
//        val endTime = LocalTime.of((lastSlot.hour + 1) % 24, 0)
//
//        return TimeRange(startTime, endTime)
//    }
//
//    /**
//     * Check if booking spans across days
//     *
//     * Examples:
//     * 23:00-01:00 = cross-day
//     * 10:00-12:00 = same day
//     */
//    private fun isCrossDayBooking(context: PricingContext): Boolean {
//        val timeRange = createTimeRangeFromSlots(context)
//        val startTime = timeRange.startTime.toLocalTime()
//        val endTime = timeRange.endTime.toLocalTime()
//
//        return startTime >= endTime
//    }
//
//
//
//    private fun formatDuration(duration: Duration): String {
//        val hours = duration.toHours()
//        val minutes = duration.toMinutesPart()
//        return "${hours}h ${minutes}min"
//    }
//
//}