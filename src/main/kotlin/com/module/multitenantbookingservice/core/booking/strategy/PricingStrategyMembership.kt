package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyMembership(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = Strategy.MEMBERSHIP.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        return PricingResult(currentResult.items)
    }
}