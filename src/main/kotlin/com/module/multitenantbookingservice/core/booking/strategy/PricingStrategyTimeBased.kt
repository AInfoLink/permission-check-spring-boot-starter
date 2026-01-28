package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyTimeBased(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = "TimeBased"

    override fun calculatePrice(context: PricingContext): PricingItemResult {
        TODO("Not yet implemented")
    }
}