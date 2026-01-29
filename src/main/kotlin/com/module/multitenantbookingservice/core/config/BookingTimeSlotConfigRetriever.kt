package com.module.multitenantbookingservice.core.config

import com.module.multitenantbookingservice.core.repository.DynamicConfigRepository
import com.module.multitenantbookingservice.core.strategy.BookingTimeSlotConfig
import com.module.multitenantbookingservice.core.strategy.TimeSlotInterval
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class BookingTimeSlotConfigRetriever(
    private val dynamicConfigRetriever: DynamicConfigRepository,
    private val mapper: ConfigMapperService
): DynamicConfigRetriever<BookingTimeSlotConfig> {
    override fun getConfig(tenantId: UUID): BookingTimeSlotConfig {
        val dynamicConfig = dynamicConfigRetriever.findByTenantIdAndKey(tenantId, BookingTimeSlotConfig.CONFIG_KEY).getOrNull()
            ?: return BookingTimeSlotConfig().withDefaultConfig(TimeSlotInterval.HOURLY)
        return mapper.convert(dynamicConfig, BookingTimeSlotConfig::class.java)
    }
}