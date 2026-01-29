package com.module.multitenantbookingservice.core.config

import com.module.multitenantbookingservice.core.config.tenant.BookingTimeSlotConfigRetriever
import com.module.multitenantbookingservice.core.strategy.BookingTimeSlotConfig
import com.module.multitenantbookingservice.core.strategy.TimeSlotInterval
import org.springframework.stereotype.Service
import java.util.*


@Service
class TenantConfigInitializer(
    val bookingTimeSlotConfigRetriever: BookingTimeSlotConfigRetriever
) {

    fun initialize(tenantId: UUID) {
        // Initialization logic for tenant configurations
        println("TenantConfigInitializer has been initialized.")
        bookingTimeSlotConfigRetriever.getConfig(tenantId)
        val bookingTimeSlotConfig = BookingTimeSlotConfig().withDefaultConfig(TimeSlotInterval.HOURLY)

    }
}