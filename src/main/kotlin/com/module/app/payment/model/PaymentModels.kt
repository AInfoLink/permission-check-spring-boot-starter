package com.module.app.payment.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 付款請求
 */
data class PaymentRequest(
    /**
     * 商戶訂單號
     */
    val merchantOrderId: String,

    /**
     * 付款金額
     */
    val amount: Int,

    /**
     * 付款描述
     */
    val description: String,

    /**
     * 付款人資訊
     */
    val payer: PayerContext? = null,

    /**
     * 回調URL
     */
    val callbackUrl: String,

    /**
     * 成功返回URL
     */
    val returnUrl: String? = null,

    /**
     * 失敗返回URL
     */
    val cancelUrl: String? = null,

    /**
     * 額外元數據
     */
    val metadata: Map<String, String> = emptyMap()
)

/**
 * 付款人資訊
 */
data class PayerContext(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null
)

/**
 * 付款初始化結果
 */
data class PaymentInitiationResult(
    /**
     * 是否成功初始化
     */
    val success: Boolean,

    /**
     * 金流系統的交易ID
     */
    val paymentId: String?,

    /**
     * 商戶訂單號
     */
    val merchantOrderId: String,

    /**
     * 金額
     */
    val amount: Int,

    /**
     * 金流系統回傳的原始數據（用於後續處理）
     */
    val rawData: Map<String, Any>? = null,

    /**
     * 錯誤訊息（當 success = false 時）
     */
    val errorMessage: String? = null,

    /**
     * 錯誤代碼
     */
    val errorCode: String? = null
)

/**
 * 金流回調數據
 */
data class PaymentCallback(
    /**
     * 金流系統的交易ID
     */
    val paymentId: String?,

    /**
     * 商戶訂單號
     */
    val merchantOrderId: String?,

    /**
     * 回調類型
     */
    val type: CallbackType,

    /**
     * 原始回調數據
     */
    val rawData: Map<String, Any>,

    /**
     * 回調時間
     */
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 回調類型
 */
enum class CallbackType {
    /**
     * 付款成功
     */
    PAYMENT_SUCCESS,

    /**
     * 付款失敗
     */
    PAYMENT_FAILED,

    /**
     * 付款取消
     */
    PAYMENT_CANCELLED,

    /**
     * 退款通知
     */
    REFUND_NOTIFICATION,

    /**
     * 其他通知
     */
    OTHER
}

/**
 * 付款結果
 */
data class PaymentResult(
    /**
     * 處理狀態
     */
    val status: PaymentStatus,

    /**
     * 金流系統的交易ID
     */
    val paymentId: String?,

    /**
     * 商戶訂單號
     */
    val merchantOrderId: String,

    /**
     * 實際付款金額
     */
    val paidAmount: BigDecimal? = null,

    /**
     * 幣別
     */
    val currency: String,

    /**
     * 付款時間
     */
    val paidAt: LocalDateTime? = null,

    /**
     * 金流系統的交易號碼
     */
    val transactionId: String? = null,

    /**
     * 錯誤訊息
     */
    val errorMessage: String? = null,

    /**
     * 錯誤代碼
     */
    val errorCode: String? = null,

    /**
     * 原始數據
     */
    val rawData: Map<String, Any>? = null
)

/**
 * 付款狀態
 */
enum class PaymentStatus {
    /**
     * 處理中（等待用戶付款）
     */
    PENDING,

    /**
     * 付款成功
     */
    SUCCESS,

    /**
     * 付款失敗
     */
    FAILED,

    /**
     * 已取消
     */
    CANCELLED,

    /**
     * 已退款
     */
    REFUNDED,

    /**
     * 部分退款
     */
    PARTIALLY_REFUNDED,

    /**
     * 未知狀態（需要查詢）
     */
    UNKNOWN
}

/**
 * 付款查詢請求
 */
data class PaymentQuery(
    /**
     * 金流系統的交易ID
     */
    val paymentId: String? = null,

    /**
     * 商戶訂單號
     */
    val merchantOrderId: String? = null,

    /**
     * 查詢時間範圍
     */
    val dateRange: DateRange? = null
)

/**
 * 時間範圍
 */
data class DateRange(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

/**
 * 退款請求
 */
data class RefundRequest(
    /**
     * 原始付款的交易ID
     */
    val paymentId: String,

    /**
     * 商戶訂單號
     */
    val merchantOrderId: String,

    /**
     * 退款金額（null 表示全額退款）
     */
    val amount: Int? = null,

    /**
     * 退款原因
     */
    val reason: String? = null,

    /**
     * 退款註記
     */
    val note: String? = null
)

/**
 * 退款結果
 */
data class RefundResult(
    /**
     * 退款是否成功
     */
    val success: Boolean,

    /**
     * 退款ID
     */
    val refundId: String? = null,

    /**
     * 原始付款ID
     */
    val paymentId: String,

    /**
     * 退款金額
     */
    val refundAmount: BigDecimal? = null,

    /**
     * 幣別
     */
    val currency: String,

    /**
     * 退款狀態
     */
    val status: RefundStatus,

    /**
     * 錯誤訊息
     */
    val errorMessage: String? = null,

    /**
     * 錯誤代碼
     */
    val errorCode: String? = null,

    /**
     * 原始數據
     */
    val rawData: Map<String, Any>? = null
)

/**
 * 退款狀態
 */
enum class RefundStatus {
    /**
     * 退款中
     */
    PROCESSING,

    /**
     * 退款成功
     */
    SUCCESS,

    /**
     * 退款失敗
     */
    FAILED,

    /**
     * 退款被拒絕
     */
    REJECTED
}

/**
 * 用戶導向指令
 */
data class RedirectInstruction(
    /**
     * 導向類型
     */
    val type: RedirectType,

    /**
     * 導向內容
     */
    val payload: String,

    /**
     * 額外參數
     */
    val parameters: Map<String, String> = emptyMap()
)

/**
 * 導向類型
 */
enum class RedirectType {
    /**
     * URL 重定向
     */
    URL,

    /**
     * HTML 表單
     */
    HTML_FORM,

    /**
     * APP Deeplink
     */
    DEEPLINK,

    /**
     * QR Code 數據
     */
    QR_CODE,
}

/**
 * 對帳請求
 */
data class ReconcileRequest(
    /**
     * 對帳日期
     */
    val reconcileDate: LocalDateTime,

    /**
     * 時間範圍
     */
    val dateRange: DateRange? = null,

    /**
     * 交易類型
     */
    val transactionTypes: Set<String> = emptySet()
)

/**
 * 對帳結果
 */
data class ReconcileResult(
    /**
     * 對帳是否成功
     */
    val success: Boolean,

    /**
     * 對帳記錄
     */
    val records: List<ReconcileRecord>,

    /**
     * 總計統計
     */
    val summary: ReconcileSummary,

    /**
     * 錯誤訊息
     */
    val errorMessage: String? = null
)

/**
 * 對帳記錄
 */
data class ReconcileRecord(
    val paymentId: String,
    val merchantOrderId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val transactionTime: LocalDateTime,
    val fee: BigDecimal? = null
)

/**
 * 對帳統計
 */
data class ReconcileSummary(
    val totalCount: Int,
    val successCount: Int,
    val totalAmount: BigDecimal,
    val totalFee: BigDecimal
)