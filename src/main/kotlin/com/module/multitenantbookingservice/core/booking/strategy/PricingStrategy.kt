package com.module.multitenantbookingservice.core.booking.strategy

import com.module.multitenantbookingservice.security.model.User
import org.springframework.stereotype.Service


enum class Strategy(val strategyName: String) {
    BASE_PRICE("BasePrice"),
    TIME_BASED("TimeBased"),
    MEMBERSHIP("Membership"),
    OVERRIDE("Override")
}

data class PricingContext(
    val user: User
)

data class PricingItemResult(
    val itemName: String,
    val description: String,
    val price: Double
)

data class PricingResult(
    val items: Iterable<PricingItemResult>
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
        var currentResult = PricingResult(emptyList())

        for (strategy in sortedStrategies) {
            currentResult = strategy.calculatePrice(context, currentResult)
        }

        return currentResult
    }
}

