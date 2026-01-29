package com.module.multitenantbookingservice.core.config.tenant

import com.module.multitenantbookingservice.core.config.DynamicConfigRetriever
import com.module.multitenantbookingservice.core.config.GenericConfigRetriever
import com.module.multitenantbookingservice.core.strategy.BookingTimeSlotConfig
import com.module.multitenantbookingservice.core.strategy.TimeSlotInterval
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BookingTimeSlotConfigRetriever(
    private val genericConfigRetriever: GenericConfigRetriever
): DynamicConfigRetriever<BookingTimeSlotConfig> {
    override fun getConfig(tenantId: UUID): BookingTimeSlotConfig {
        return genericConfigRetriever.getConfig(
            tenantId = tenantId,
            configKey = BookingTimeSlotConfig.CONFIG_KEY,
            configClass = BookingTimeSlotConfig::class.java
        ) {
            BookingTimeSlotConfig().withDefaultConfig(TimeSlotInterval.HOURLY)
        }
    }
}