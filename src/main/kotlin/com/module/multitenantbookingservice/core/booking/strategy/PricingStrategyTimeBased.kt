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
        val newItems = currentResult.items.toMutableSet()
        if (context.basePrice == null) {
            throw BasePriceNotSet
        }

        val basePrice = context.basePrice!!

        // Get time slot configuration
        val timeSlotConfig = getTimeSlotConfig(context)

        // Calculate time-based price
        val timeBasedPrice = calculateTimeBasedPrice(basePrice, timeSlotConfig, context)

        // Add base price item if needed
        addBasePriceItemIfNeeded(newItems, basePrice, timeSlotConfig)

        // Add time slot adjustment item if there are adjustments
        addTimeSlotAdjustmentIfNeeded(newItems, timeBasedPrice, basePrice, timeSlotConfig, context)

        return PricingResult(newItems)
    }

    private fun getTimeSlotConfig(context: PricingContext): VenueTimeSlotConfig? {
        // Simplified version: directly find applicable time slot configuration
        return venueTimeSlotConfigRepository.findByVenueAndTime(
            context.venue,
            context.startTime.toLocalTime()
        )
    }

    private fun calculateTimeBasedPrice(
        basePrice: Double,
        config: VenueTimeSlotConfig?,
        context: PricingContext
    ): Double {
        if (config == null) {
            return basePrice // No special configuration, use base price
        }

        val hours = ceil(context.bookingDuration.toMinutes() / 60.0)
        val adjustedPrice = basePrice * config.priceMultiplier + (config.additionalFee * hours)

        return adjustedPrice
    }

    private fun addBasePriceItemIfNeeded(
        items: MutableSet<PricingItemResult>,
        basePrice: Double,
        config: VenueTimeSlotConfig?
    ) {
        val hasBasePrice = items.any { it.itemName.contains("Base Price", ignoreCase = true) }

        if (!hasBasePrice) {
            val basePriceDescription = when (config?.slotType) {
                TimeSlotType.PEAK -> "Standard base rate (before peak hour adjustment)"
                TimeSlotType.UNATTENDED -> "Standard base rate (before off-peak discount)"
                else -> "Standard booking fee"
            }

            items.add(PricingItemResult(
                itemName = "Base Price",
                description = basePriceDescription,
                price = basePrice
            ))
        }
    }

    private fun addTimeSlotAdjustmentIfNeeded(
        items: MutableSet<PricingItemResult>,
        timeBasedPrice: Double,
        basePrice: Double,
        config: VenueTimeSlotConfig?,
        context: PricingContext
    ) {
        if (config == null) {
            return // No adjustment
        }

        val adjustment = timeBasedPrice - basePrice

        if (adjustment != 0.0) {
            val adjustmentItem = PricingItemResult(
                itemName = "${config.slotType.typeName} Adjustment",
                description = buildTimeSlotDescription(config, context),
                price = adjustment
            )

            items.add(adjustmentItem)
        }
    }

    private fun buildTimeSlotDescription(config: VenueTimeSlotConfig, context: PricingContext): String {
        val timeRange = "${config.startTime} - ${config.endTime}"
        val durationInfo = " (${context.bookingDuration.toHours()}h ${context.bookingDuration.toMinutesPart()}min)"

        return when (config.slotType) {
            TimeSlotType.PEAK -> "Peak hour surcharge $timeRange$durationInfo (${config.priceMultiplier}x)"
            TimeSlotType.UNATTENDED -> "Off-peak discount $timeRange$durationInfo (${config.priceMultiplier}x)"
            TimeSlotType.REGULAR -> "Regular rate $timeRange$durationInfo (${config.priceMultiplier}x)"
        }
    }
}