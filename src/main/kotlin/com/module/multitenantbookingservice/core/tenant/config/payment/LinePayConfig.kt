package com.module.multitenantbookingservice.core.tenant.config.payment

import com.module.multitenantbookingservice.commons.ValidationRequired

data class LinePayConfig(
    var channelId: String,
    var channelSecret: String,
    var apiBaseUrl: String
): ValidationRequired {

    companion object {
        val CONFIG_KEY = "payment.linepay.config"

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