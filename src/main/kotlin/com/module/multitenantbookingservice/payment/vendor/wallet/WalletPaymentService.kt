package com.module.multitenantbookingservice.payment.vendor.wallet

import com.module.multitenantbookingservice.core.tenant.config.payment.WalletConfig
import com.module.multitenantbookingservice.payment.PaymentService
import com.module.multitenantbookingservice.payment.PaymentServiceType
import com.module.multitenantbookingservice.payment.model.PaymentCallback
import com.module.multitenantbookingservice.payment.model.PaymentInitiationResult
import com.module.multitenantbookingservice.payment.model.PaymentRequest
import com.module.multitenantbookingservice.payment.model.PaymentResult
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