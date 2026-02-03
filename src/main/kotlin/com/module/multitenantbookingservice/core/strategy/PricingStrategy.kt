package com.module.multitenantbookingservice.core.strategy

import com.module.multitenantbookingservice.core.models.Venue
import com.module.multitenantbookingservice.core.tenant.config.BookingTimeSlotConfig
import com.module.multitenantbookingservice.security.model.User
import org.springframework.stereotype.Service


enum class Strategy(val strategyName: String) {
    CROSS_DAY_HELPER("CrossDayHelper"),
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
    val user: User,
    val venue: Venue,
    val bookingTimeSlots: MutableSet<BookingTimeSlotView>,
    val bookingTimeSlotConfig: BookingTimeSlotConfig,
) {
}

data class PricingItemResult(
    val timeRange: TimeRange,
    val itemName: String,
    val description: String,
    val price: Double
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

