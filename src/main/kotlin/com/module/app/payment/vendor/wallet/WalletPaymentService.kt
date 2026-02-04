package com.module.app.payment.vendor.wallet

import com.module.app.core.tenant.config.payment.WalletConfig
import com.module.app.payment.PaymentService
import com.module.app.payment.PaymentServiceType
import com.module.app.payment.model.PaymentCallback
import com.module.app.payment.model.PaymentInitiationResult
import com.module.app.payment.model.PaymentRequest
import com.module.app.payment.model.PaymentResult
import org.springframework.stereotype.Service


@Service
class WalletPaymentService: PaymentService<WalletConfig> {
    override fun getPaymentConfig(): WalletConfig {
        TODO("Not yet implemented")
    }

    override fun initiatePayment(request: PaymentRequest): PaymentInitiationResult {
        TODO("Not yet implemented")
    }

    override fun handlePaymentCallback(callback: PaymentCallback, hookAction: (() -> Unit)?): PaymentResult {
        TODO("Not yet implemented")
    }

    override fun getServiceType(): PaymentServiceType {
        return PaymentServiceType.WALLET
    }
}