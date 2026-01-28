package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyMembership(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = Strategy.MEMBERSHIP.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        // Membership strategy: applies discount based on current total
        val currentTotal = currentResult.getTotalAmount()
        val discountRate = 0.1 // 10% discount for members
        val discountAmount = currentTotal * discountRate

        val membershipDiscountItem = PricingItemResult(
            itemName = "Membership Discount",
            description = "10% discount for members",
            price = -discountAmount // Negative price for discount
        )

        // Add discount to existing items
        val newItems = currentResult.items.toMutableList()
        newItems.add(membershipDiscountItem)

        return PricingResult(newItems)
    }
}