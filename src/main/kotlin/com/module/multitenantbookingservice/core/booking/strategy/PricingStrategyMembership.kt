package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyMembership(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = "Membership"

    override fun calculatePrice(context: PricingContext): PricingItemResult {
        TODO("Not yet implemented")
    }
}