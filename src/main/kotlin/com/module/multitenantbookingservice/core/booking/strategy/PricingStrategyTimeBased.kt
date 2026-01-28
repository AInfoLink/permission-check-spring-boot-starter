package com.module.multitenantbookingservice.core.booking.strategy

import com.module.multitenantbookingservice.core.models.TimeSlotType
import com.module.multitenantbookingservice.core.models.VenueTimeSlotConfig
import com.module.multitenantbookingservice.core.repository.VenueTimeSlotConfigRepository
import com.module.multitenantbookingservice.security.BasePriceNotSet
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class PricingStrategyTimeBased(
    private val venueTimeSlotConfigRepository: VenueTimeSlotConfigRepository
) : PricingStrategy {

    override var priority: Int = 1 // Set default priority

    override val name: String
        get() = Strategy.TIME_BASED.strategyName

    override fun calculatePrice(context: PricingContext, currentResult: PricingResult): PricingResult {
        return PricingResult(mutableSetOf())
    }
}