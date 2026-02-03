package com.module.multitenantbookingservice.payment.vendor.line

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import java.util.*


@ConfigurationProperties(prefix = "payment.vendor.line")
data class LineProperties(
    val baseUrl: String,
    val channelId: String,
    val authorizationToken: String,
    val merchantDeviceProfileId: String
)

@Configuration
class LineFeignConfig(
    val lineProperties: LineProperties
) {
    @Bean
    fun lineHeaderInterceptor(): RequestInterceptor {
        return RequestInterceptor { template: RequestTemplate ->
            template.header("Content-Type", "application/json")
            template.header("X-LINE-Authorization", lineProperties.authorizationToken)
            template.header("X-LINE-Authorization-Nonce", UUID.randomUUID().toString())
            template.header("X-LINE-ChannelId", lineProperties.channelId)
        }
    }
}

@FeignClient(
    name = "lineClient",
    url = "\${payment.vendor.line.base-url}",
    configuration = [LineFeignConfig::class]
)
interface LineClient {

    @PostMapping("/v3/payments/request")
    fun requestPayment(@RequestBody request: LinePaymentRequest): LinePaymentResponse

    @PostMapping("/v3/payments/{transactionId}/confirm")
    fun confirmPayment(
        @PathVariable transactionId: String,
        @RequestBody request: LinePaymentConfirmRequest
    ): LinePaymentConfirmResponse

    @PostMapping("/v3/payments/authorizations/{transactionId}/void")
    fun voidPayment(@PathVariable transactionId: String): LinePaymentVoidResponse
}

// Request/Response Data Classes
data class LinePaymentRequest(
    val amount: Long,
    val currency: String,
    val orderId: String,
    val packages: List<LinePackage>,
    val redirectUrls: LineRedirectUrls
)

data class LinePackage(
    val id: String,
    val amount: Long,
    val products: List<LineProduct>
)

data class LineProduct(
    val name: String,
    val quantity: Int,
    val price: Long
)

data class LineRedirectUrls(
    val confirmUrl: String,
    val cancelUrl: String
)

data class LinePaymentResponse(
    val returnCode: String,
    val returnMessage: String,
    val info: LinePaymentInfo?
)

data class LinePaymentInfo(
    val paymentUrl: LinePaymentUrl,
    val transactionId: String,
    val paymentAccessToken: String
)

data class LinePaymentUrl(
    val web: String,
    val app: String
)

data class LinePaymentConfirmRequest(
    val amount: Long,
    val currency: String
)

data class LinePaymentConfirmResponse(
    val returnCode: String,
    val returnMessage: String,
    val info: LineConfirmInfo?
)

data class LineConfirmInfo(
    val orderId: String,
    val transactionId: String,
    val payInfo: List<LinePayInfo>
)

data class LinePayInfo(
    val method: String,
    val amount: Long
)

data class LinePaymentVoidResponse(
    val returnCode: String,
    val returnMessage: String
)