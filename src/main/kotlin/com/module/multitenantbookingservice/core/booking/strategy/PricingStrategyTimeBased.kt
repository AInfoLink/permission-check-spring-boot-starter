package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyTimeBased(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = Strategy.TIME_BASED.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        // Time-based strategy: adds time-related fees (peak hours, etc.)
        val timeFeeItem = PricingItemResult(
            itemName = "Time Fee",
            description = "Additional fee for booking time",
            price = 25.0 // TODO: Should be calculated based on booking time
        )

        // Add to existing items
        val newItems = currentResult.items.toMutableList()
        newItems.add(timeFeeItem)

        return PricingResult(newItems)
    }
}