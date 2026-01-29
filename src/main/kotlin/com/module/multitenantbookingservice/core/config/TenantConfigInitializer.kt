package com.module.multitenantbookingservice.core.config

import com.module.multitenantbookingservice.core.config.tenant.BookingTimeSlotConfigRetriever
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import java.util.*


@Service
class TenantConfigInitializer(
    val bookingTimeSlotConfigRetriever: BookingTimeSlotConfigRetriever
) {
    @PostConstruct
    fun postConstruct() {
        println("TenantConfigInitializer postConstruct called.")
    }

    fun initialize(tenantId: UUID) {
        // Initialization logic for tenant configurations
        println("TenantConfigInitializer has been initialized.")

    }
}