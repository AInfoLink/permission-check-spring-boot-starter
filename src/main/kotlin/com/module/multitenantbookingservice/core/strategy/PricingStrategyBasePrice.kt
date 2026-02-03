package com.module.multitenantbookingservice.core.strategy

import com.module.multitenantbookingservice.core.tenant.config.BookingTimeSlot

class PricingStrategyBasePrice(override var priority: Int) : PricingStrategy {
    override val name: String
        get() = Strategy.BASE_PRICE.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        val bookingTimeSlots: MutableSet<BookingTimeSlot> = mutableSetOf()
        context.bookingTimeSlots.forEach {
            val bookingTimeSlot =  context.bookingTimeSlotConfig.querySlot(it.hour)
            bookingTimeSlot.isHalfHour = it.isHalfHour
            bookingTimeSlots.add(bookingTimeSlot)
        }
        bookingTimeSlots.forEach { slot ->
            val timeRange = slot.asTimeRange()
            val itemName = "Base Price for Hour ${slot.hour}"
            val description = "Base price calculation for hour ${slot.hour}"
            val price = slot.getPriceForSlot()
            val pricingItem = PricingItemResult(
                timeRange = timeRange,
                itemName = itemName,
                description = description,
                price = price
            )
            currentResult.items.add(pricingItem)
        }
        return currentResult
    }
}