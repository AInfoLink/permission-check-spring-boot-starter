package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyBasePrice(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = Strategy.BASE_PRICE.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {

        return currentResult
    }
}