package com.module.app.core.tenant.config.payment

import com.module.app.core.tenant.service.DynamicConfigRetriever
import com.module.app.core.tenant.service.GenericConfigRetriever
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
            LinePayConfig().withDefault()
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
