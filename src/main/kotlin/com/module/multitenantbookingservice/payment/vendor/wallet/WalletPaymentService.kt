package com.module.multitenantbookingservice.payment.vendor.wallet

import com.module.multitenantbookingservice.payment.PaymentService
import com.module.multitenantbookingservice.payment.model.PaymentCallback
import com.module.multitenantbookingservice.payment.model.PaymentInitiationResult
import com.module.multitenantbookingservice.payment.model.PaymentRequest
import com.module.multitenantbookingservice.payment.model.PaymentResult

class WalletPaymentService: PaymentService {
    override fun initiatePayment(request: PaymentRequest): PaymentInitiationResult {
        TODO("Not yet implemented")
    }

    override fun handlePaymentCallback(callback: PaymentCallback): PaymentResult {
        TODO("Not yet implemented")
    }

    override fun getServiceName(): String {
        return "wallet"
    }
}