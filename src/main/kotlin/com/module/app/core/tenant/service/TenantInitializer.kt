package com.module.app.core.tenant.service

import com.module.app.core.tenant.config.booking.BookingTimeSlotConfigRetriever
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
@Service
class TenantInitializer(
    val bookingTimeSlotConfigRetriever: BookingTimeSlotConfigRetriever
) {
    private val logger = LoggerFactory.getLogger(TenantInitializer::class.java)

    @Transactional
    fun initialize(tenantId: String) {
        // Initialization logic for tenant configurations
        logger.info("Initializing configurations for tenant: $tenantId")
        val timeSlotConfig = bookingTimeSlotConfigRetriever.getConfig(tenantId)
        logger.info("Current BookingTimeSlotConfig for tenant $tenantId: $timeSlotConfig")
        bookingTimeSlotConfigRetriever.saveConfig(tenantId, timeSlotConfig)
    }
}