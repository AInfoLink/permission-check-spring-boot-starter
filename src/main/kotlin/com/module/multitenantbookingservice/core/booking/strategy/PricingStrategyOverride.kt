package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyOverride(override var priority: Int = Int.MAX_VALUE) : PricingStrategy {
    override val name: String
        get() = "Override"

    override fun calculatePrice(context: PricingContext): PricingItemResult {
        TODO("Not yet implemented")
    }

}