package com.module.multitenantbookingservice.core.strategy

import com.module.multitenantbookingservice.commons.ValidationRequired
import com.module.multitenantbookingservice.core.models.UserProfile
import com.module.multitenantbookingservice.core.tenant.config.booking.BookingTimeSlotConfig
import com.module.multitenantbookingservice.security.BookingMustBeInConsecutiveHours
import org.springframework.stereotype.Service

enum class Strategy(val strategyName: String) {
    BASE_PRICE("BasePrice"),
    TIME_BASED("TimeBased"),
    MEMBERSHIP("Membership"),
    OVERRIDE("Override")
}

data class BookingTimeSlotView(
    val hour: Int,
    val isHalfHour: Boolean,
)

data class PricingContext(
    val profile: UserProfile,
    val bookingTimeSlots: MutableSet<BookingTimeSlotView>,
    val bookingTimeSlotConfig: BookingTimeSlotConfig,
): ValidationRequired {
    override fun validate(): MutableSet<Exception> {
        val exceptions = mutableSetOf<Exception>()
        // Basic validation for consecutive time slots
        return exceptions
    }

    private fun validateConsecutiveHours(): Exception? {
        val sortedHours = bookingTimeSlots.map { it.hour }.sorted()
        for (idx in 1 until sortedHours.size) {
            if (sortedHours[idx] != sortedHours[idx - 1] + 1) {
                return BookingMustBeInConsecutiveHours.withDetails(
                    "Booking time slots must be consecutive hours. Found gap between ${sortedHours[idx - 1]} and ${sortedHours[idx]}"
                )
            }
        }
        return null
    }
}

data class PricingItemResult(
    val timeRange: TimeRange,
    val itemName: String,
    val description: String,
    var price: Double
)

data class PricingResult(
    val items: MutableSet<PricingItemResult>
) {
    fun getTotalAmount(): Double {
        return items.sumOf { it.price }
    }
}



interface PricingStrategy {
    val name: String
    var priority: Int
    fun supports(strategyName: String): Boolean {
        return strategyName == name
    }
    fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult
}


interface PricingStrategyEngine {
    fun calculatePrice(context: PricingContext, strategies: Iterable<PricingStrategy>): PricingResult
}


@Service
class DefaultPricingStrategyEngine : PricingStrategyEngine {
    override fun calculatePrice(
        context: PricingContext,
        strategies: Iterable<PricingStrategy>
    ): PricingResult {
        val sortedStrategies = strategies.sortedBy { it.priority }

        // Start with empty result and let each strategy build upon it
        var currentResult = PricingResult(mutableSetOf())

        for (strategy in sortedStrategies) {
            currentResult = strategy.calculatePrice(context, currentResult)
        }

        return currentResult
    }
}

