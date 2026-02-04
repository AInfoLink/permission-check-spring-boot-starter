package com.module.app.core.tenant.config.booking

import com.module.app.core.tenant.service.DynamicConfigRetriever
import com.module.app.core.tenant.service.GenericConfigRetriever
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
            BookingTimeSlotConfig.default()
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