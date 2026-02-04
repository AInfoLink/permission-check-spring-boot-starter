package com.module.app.payment

import com.module.app.payment.capability.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 付款閘道 - 付款服務的統一入口
 *
 * 責任：
 * 1. 路由：根據條件選擇合適的付款服務
 * 2. 管理：維護所有可用的付款服務
 * 3. 裝飾：提供統一的日誌、監控、異常處理
 *
 * 注意：這個 Gateway 不參與具體付款流程，只做路由和管理
 */
@Component
class PaymentGateway(
    private val paymentServices: List<PaymentService<*>>
) {

    private val logger = LoggerFactory.getLogger(PaymentGateway::class.java)

    /**
     * 根據服務名稱取得付款服務
     *
     * @param serviceType 服務名稱（如：linepay, ecpay）
     * @return 對應的付款服務
     * @throws PaymentServiceNotFoundException 找不到服務時拋出
     */
    fun getPaymentService(serviceType: PaymentServiceType): PaymentService<*> {
        return paymentServices.find { it.getServiceType() == serviceType }
            ?: throw PaymentServiceNotFoundException("Payment service not found: $serviceType")
    }

    /**
     * 取得所有可用的付款服務
     */
    fun getAvailableServices(): List<PaymentServiceContext> {
        return paymentServices.map { service ->
            PaymentServiceContext(
                serviceType = service.getServiceType(),
                capabilities = getServiceCapabilities(service)
            )
        }
    }

    /**
     * 檢查服務是否支援特定能力
     */
    fun hasCapability(serviceType: PaymentServiceType, capability: PaymentCapabilityType): Boolean {
        val service = getPaymentService(serviceType)
        return when (capability) {
            PaymentCapabilityType.REFUND -> service is RefundCapability
            PaymentCapabilityType.QUERY -> service is QueryCapability
            PaymentCapabilityType.REDIRECT -> service is RedirectCapability
            PaymentCapabilityType.SIGNATURE -> service is SignatureCapability
            PaymentCapabilityType.RECONCILE -> service is ReconcileCapability
        }
    }

    /**
     * 安全地執行付款操作（帶錯誤處理和日誌）
     */
    fun <T> executeWithErrorHandling(
        serviceName: String,
        operation: String,
        block: () -> T
    ): T {
        logger.debug("Executing {} operation for service: {}", operation, serviceName)
        return try {
            block().also {
                logger.debug("Successfully executed {} operation for service: {}",
                           operation, serviceName)
            }
        } catch (e: Exception) {
            logger.error("Failed to execute {} operation for service: {}",
                        operation, serviceName, e)
            throw PaymentOperationException(
                "Failed to execute $operation for service $serviceName", e
            )
        }
    }

    private fun getServiceCapabilities(service: PaymentService<*>): Set<PaymentCapabilityType> {
        val capabilities = mutableSetOf<PaymentCapabilityType>()

        if (service is RefundCapability) capabilities.add(PaymentCapabilityType.REFUND)
        if (service is QueryCapability) capabilities.add(PaymentCapabilityType.QUERY)
        if (service is RedirectCapability) capabilities.add(PaymentCapabilityType.REDIRECT)
        if (service is SignatureCapability) capabilities.add(PaymentCapabilityType.SIGNATURE)
        if (service is ReconcileCapability) capabilities.add(PaymentCapabilityType.RECONCILE)

        return capabilities
    }
}

/**
 * 付款能力類型
 */
enum class PaymentCapabilityType {
    REFUND,
    QUERY,
    REDIRECT,
    SIGNATURE,
    RECONCILE
}

/**
 * 付款服務資訊
 */
data class PaymentServiceContext(
    val serviceType: PaymentServiceType,
    val capabilities: Set<PaymentCapabilityType>
)

/**
 * 付款相關異常
 */
sealed class PaymentException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

class PaymentServiceNotFoundException(message: String) : PaymentException(message)

class NoSuitablePaymentServiceException(message: String) : PaymentException(message)

class PaymentOperationException(message: String, cause: Throwable? = null) :
    PaymentException(message, cause)