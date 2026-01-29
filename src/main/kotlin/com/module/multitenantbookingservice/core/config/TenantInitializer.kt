package com.module.multitenantbookingservice.core.config

import com.module.multitenantbookingservice.core.config.tenant.BookingTimeSlotConfigRetriever
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*


@Service
class TenantInitializer(
    val bookingTimeSlotConfigRetriever: BookingTimeSlotConfigRetriever
) {
    private val logger = LoggerFactory.getLogger(TenantInitializer::class.java)

    @Transactional
    fun initialize(tenantId: UUID) {
        // Initialization logic for tenant configurations
        logger.info("Initializing configurations for tenant: $tenantId")
        val timeSlotConfig = bookingTimeSlotConfigRetriever.getConfig(tenantId)
        logger.info("Current BookingTimeSlotConfig for tenant $tenantId: $timeSlotConfig")
        bookingTimeSlotConfigRetriever.saveConfig(tenantId, timeSlotConfig)
    }
}