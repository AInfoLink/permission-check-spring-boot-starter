package com.module.app.core.tenant.config.payment

import com.module.app.commons.annotation.SystemManaged
import com.module.app.commons.annotation.TenantConfig
import com.module.app.commons.contract.HasDefault
import com.module.app.commons.contract.ValidationRequired
import com.module.app.commons.utils.TenantConfigUtils


@TenantConfig("payment.linepay.config")
data class LinePayConfig(
    @SystemManaged
    var apiBaseUrl: String = "",
    var channelId: String = "",
    var channelSecret: String = "",
): ValidationRequired, HasDefault<LinePayConfig> {
    companion object {
        val CONFIG_KEY = TenantConfigUtils.getConfigKey(LinePayConfig::class)
    }

    override fun validate(): MutableSet<Exception> {
        val errors = mutableSetOf<Exception>()
        return errors
    }

    override fun withDefault(): LinePayConfig {
        return LinePayConfig()
    }
}