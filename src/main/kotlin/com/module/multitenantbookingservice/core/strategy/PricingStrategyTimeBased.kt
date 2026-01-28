package com.module.multitenantbookingservice.core.strategy

import org.springframework.stereotype.Component

@Component
class PricingStrategyTimeBased(
) : PricingStrategy {

    override var priority: Int = 1 // Set default priority

    override val name: String
        get() = Strategy.TIME_BASED.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        return PricingResult(mutableSetOf())
    }
}