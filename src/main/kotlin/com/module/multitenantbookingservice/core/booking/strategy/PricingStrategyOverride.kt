package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyOverride(override var priority: Int = Int.MAX_VALUE) : PricingStrategy {
    override val name: String
        get() = Strategy.OVERRIDE.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        // Override strategy: completely replaces all previous pricing
        val overrideItem = PricingItemResult(
            itemName = "Special Override Price",
            description = "Custom pricing override",
            price = 200.0 // TODO: Should be configurable or based on specific rules
        )

        // Return completely new result, ignoring previous calculations
        return PricingResult(mutableSetOf(overrideItem))
    }

}