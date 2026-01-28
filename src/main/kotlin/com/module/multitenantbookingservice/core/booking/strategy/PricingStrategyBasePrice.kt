package com.module.multitenantbookingservice.core.booking.strategy

class PricingStrategyBasePrice(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = Strategy.BASE_PRICE.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        // Base price strategy: sets the foundation price
        val basePriceItem = PricingItemResult(
            itemName = "Base Price",
            description = "Standard booking fee",
            price = 100.0 // TODO: Should be configurable
        )

        // Add to current items (usually empty for base price, but allows chaining)
        val newItems = currentResult.items.toMutableList()
        newItems.add(basePriceItem)

        return PricingResult(newItems)
    }
}