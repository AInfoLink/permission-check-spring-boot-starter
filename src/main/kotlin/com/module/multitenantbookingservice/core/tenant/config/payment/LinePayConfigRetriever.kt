package com.module.multitenantbookingservice.core.tenant.config.payment

import com.module.multitenantbookingservice.core.tenant.service.DynamicConfigRetriever
import com.module.multitenantbookingservice.core.tenant.service.GenericConfigRetriever
import org.springframework.stereotype.Service


@Service
class LinePayConfigRetriever(
    private val genericConfigRetriever: GenericConfigRetriever
): DynamicConfigRetriever<LinePayConfig> {
    override fun getConfig(tenantId: String): LinePayConfig {
        return genericConfigRetriever.getConfig(
            tenantId = tenantId,
            configKey = LinePayConfig.CONFIG_KEY,
            configClass = LinePayConfig::class.java
        ) {
            LinePayConfig.default()
        }
    }

    override fun saveConfig(tenantId: String, config: LinePayConfig) {
        genericConfigRetriever.saveConfig(
            tenantId = tenantId,
            configKey = LinePayConfig.CONFIG_KEY,
            config = config
        )
    }
}
