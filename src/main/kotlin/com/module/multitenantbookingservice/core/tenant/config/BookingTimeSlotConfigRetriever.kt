package com.module.multitenantbookingservice.core.tenant.config

import com.module.multitenantbookingservice.core.tenant.service.DynamicConfigRetriever
import com.module.multitenantbookingservice.core.tenant.service.GenericConfigRetriever
import org.springframework.stereotype.Service
@Service
class BookingTimeSlotConfigRetriever(
    private val genericConfigRetriever: GenericConfigRetriever
): DynamicConfigRetriever<BookingTimeSlotConfig> {

    /**
     * Get booking time slot configuration for specific tenant
     */
    override fun getConfig(tenantId: String): BookingTimeSlotConfig {
        return genericConfigRetriever.getConfig(
            tenantId = tenantId,
            configKey = BookingTimeSlotConfig.CONFIG_KEY,
            configClass = BookingTimeSlotConfig::class.java
        ) {
            BookingTimeSlotConfig().withDefault(TimeSlotInterval.HOURLY)
        }
    }

    /**
     * Save booking time slot configuration for specific tenant
     */
    override fun saveConfig(tenantId: String, config: BookingTimeSlotConfig) {
        genericConfigRetriever.saveConfig(
            tenantId = tenantId,
            configKey = BookingTimeSlotConfig.CONFIG_KEY,
            config = config
        )
    }

}