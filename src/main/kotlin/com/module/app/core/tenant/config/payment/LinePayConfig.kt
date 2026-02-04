package com.module.app.core.tenant.config.payment

import com.module.app.commons.annotation.SystemManaged
import com.module.app.commons.annotation.TenantConfig
import com.module.app.commons.contract.ValidationRequired
import com.module.app.commons.utils.TenantConfigUtils


@TenantConfig("payment.linepay.config")
data class LinePayConfig(
    @SystemManaged
    var apiBaseUrl: String,
    var channelId: String,
    var channelSecret: String,
): ValidationRequired {

    companion object {
        val CONFIG_KEY = TenantConfigUtils.getConfigKey(LinePayConfig::class)
        fun default(): LinePayConfig {
            return LinePayConfig(
                channelId = "",
                channelSecret = "",
                apiBaseUrl = ""
            )
        }
    }

    override fun validate(): MutableSet<Exception> {
        val errors = mutableSetOf<Exception>()
        return errors
    }
}