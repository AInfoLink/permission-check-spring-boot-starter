package com.module.multitenantbookingservice.core.strategy

class PricingStrategyMembership(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = Strategy.MEMBERSHIP.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        currentResult.items.forEach { item ->
            // Returns the discount percentage associated with the user's membership. No membership means no discount (0%)
            val discount = item.price * context.profile.getMembershipDiscountPercentageOff()
            val discountedPrice = item.price - discount
            item.price = discountedPrice
        }
        return currentResult
    }
}