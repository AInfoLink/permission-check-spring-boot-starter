package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyBasePrice(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = "BasePrice"

    override fun calculatePrice(context: PricingContext): PricingItemResult {
        TODO("Not yet implemented")
    }
}