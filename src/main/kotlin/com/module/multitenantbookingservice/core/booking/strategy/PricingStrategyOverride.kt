package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyOverride(override var priority: Int = Int.MAX_VALUE) : PricingStrategy {
    override val name: String
        get() = Strategy.OVERRIDE.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        // Override strategy: completely replaces all previous pricing s
        return PricingResult(mutableSetOf())
    }

}