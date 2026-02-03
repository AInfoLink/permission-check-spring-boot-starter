package com.module.multitenantbookingservice.payment.vendor.line

import com.module.multitenantbookingservice.payment.PaymentService
import com.module.multitenantbookingservice.payment.model.*
import org.springframework.stereotype.Service


@Service
class LinePaymentService(
    private val lineClient: LineClient
): PaymentService {

    override fun initiatePayment(request: PaymentRequest): PaymentInitiationResult {
        val lineRequest = buildLinePaymentRequest(request)
        val response = lineClient.requestPayment(lineRequest)

        return if (response.returnCode == "0000") {
            PaymentInitiationResult(
                success = true,
                paymentId = response.info?.transactionId,
                merchantOrderId = request.merchantOrderId,
                amount = request.amount,
                rawData = mapOf(
                    "paymentUrl" to (response.info?.paymentUrl?.web ?: ""),
                    "paymentAccessToken" to (response.info?.paymentAccessToken ?: ""),
                    "appUrl" to (response.info?.paymentUrl?.app ?: "")
                )
            )
        } else {
            PaymentInitiationResult(
                success = false,
                paymentId = null,
                merchantOrderId = request.merchantOrderId,
                amount = request.amount,
                errorCode = response.returnCode,
                errorMessage = response.returnMessage
            )
        }
    }

    override fun handlePaymentCallback(callback: PaymentCallback): PaymentResult {
        return try {
            // Extract amount from rawData since PaymentCallback doesn't have amount directly
            val amount = (callback.rawData["amount"] as? Number)?.toLong() ?: 0L
            val transactionId = callback.paymentId ?: ""

            val confirmRequest = LinePaymentConfirmRequest(
                amount = amount,
                currency = "TWD"
            )
            val response = lineClient.confirmPayment(transactionId, confirmRequest)

            if (response.returnCode == "0000") {
                PaymentResult(
                    status = PaymentStatus.SUCCESS,
                    paymentId = response.info?.transactionId,
                    merchantOrderId = callback.merchantOrderId ?: "",
                    paidAmount = amount.toBigDecimal(),
                    currency = "TWD",
                    transactionId = response.info?.transactionId,
                    rawData = mapOf(
                        "payInfo" to (response.info?.payInfo ?: emptyList()),
                        "orderId" to (response.info?.orderId ?: "")
                    )
                )
            } else {
                PaymentResult(
                    status = PaymentStatus.FAILED,
                    paymentId = transactionId,
                    merchantOrderId = callback.merchantOrderId ?: "",
                    currency = "TWD",
                    errorCode = response.returnCode,
                    errorMessage = response.returnMessage
                )
            }
        } catch (e: Exception) {
            PaymentResult(
                status = PaymentStatus.FAILED,
                paymentId = callback.paymentId,
                merchantOrderId = callback.merchantOrderId ?: "",
                currency = "TWD",
                errorCode = "INTERNAL_ERROR",
                errorMessage = "Payment confirmation failed: ${e.message}"
            )
        }
    }

    override fun getServiceName(): String {
        return "line"
    }

    private fun generatePackageId(): String {
        return "package-${System.currentTimeMillis()}"
    }


    private fun buildLinePaymentRequest(request: PaymentRequest): LinePaymentRequest {
        return LinePaymentRequest(
            amount = request.amount.toLong(),
            currency = "TWD",
            orderId = request.merchantOrderId,
            packages = listOf(
                LinePackage(
                    id = generatePackageId(),
                    amount = request.amount.toLong(),
                    products = listOf(
                        LineProduct(
                            name = request.description,
                            quantity = 1,
                            price = request.amount.toLong()
                        )
                    )
                )
            ),
            redirectUrls = LineRedirectUrls(
                confirmUrl = request.returnUrl ?: request.callbackUrl,
                cancelUrl = request.cancelUrl ?: request.callbackUrl
            )
        )
    }
}