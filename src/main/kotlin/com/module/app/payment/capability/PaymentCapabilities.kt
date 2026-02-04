package com.module.app.payment.capability

import com.module.app.payment.model.*

/**
 * 查詢能力（強烈建議實作）
 *
 * 用於：
 * - 補償機制：當 callback 遺失時查詢真實狀態
 * - 對帳功能：驗證交易狀態一致性
 * - 狀態確認：補強 callback 的可靠性
 *
 * 注意：query 是「輔助確認」，不是狀態轉移的核心
 */
interface QueryCapability {

    /**
     * 查詢交易狀態
     *
     * @param query 查詢條件
     * @return 付款狀態資訊
     */
    fun queryPaymentStatus(query: PaymentQuery): PaymentResult
}

/**
 * 退款能力（選用）
 *
 * 提供退款功能，包含：
 * - 全額退款 / 部分退款
 * - 單次 / 多次退款（依 vendor 能力而定）
 *
 * 注意：
 * - 退款限制透過 RefundResult 反映，不是透過型別限制
 * - 各 vendor 的退款政策差異很大，這裡提供統一介面
 */
interface RefundCapability {

    /**
     * 執行退款（不保證即時入帳）
     *
     * @param request 退款請求
     * @return 退款結果，包含是否成功和相關資訊
     */
    fun refund(request: RefundRequest): RefundResult

    /**
     * 查詢退款狀態
     *
     * @param refundId 退款ID
     * @return 退款狀態
     */
    fun queryRefundStatus(refundId: String): RefundResult

    /**
     * 取得退款限制資訊
     *
     * @param paymentId 原始付款ID
     * @return 退款限制（可退金額、次數限制等）
     */
    fun getRefundLimits(paymentId: String): RefundLimits
}

/**
 * 退款限制
 */
data class RefundLimits(
    /**
     * 可退款金額
     */
    val refundableAmount: java.math.BigDecimal,

    /**
     * 是否支援部分退款
     */
    val supportsPartialRefund: Boolean,

    /**
     * 是否支援多次退款
     */
    val supportsMultipleRefunds: Boolean,

    /**
     * 退款截止時間
     */
    val refundDeadline: java.time.LocalDateTime? = null,

    /**
     * 額外限制說明
     */
    val restrictions: List<String> = emptyList()
)

/**
 * Redirect / UI 導向能力（選用）
 *
 * 處理需要用戶互動的付款流程：
 * - URL 重定向
 * - HTML 表單
 * - App Deeplink
 * - QR Code
 */
interface RedirectCapability {

    /**
     * 取得使用者導向資訊
     *
     * 根據 PaymentInitiationResult 產生用戶導向指令
     *
     * @param initiationResult 付款初始化結果
     * @return 導向指令（URL、HTML、Deeplink 等）
     */
    fun getRedirectInstruction(initiationResult: PaymentInitiationResult): RedirectInstruction

    /**
     * 驗證導向指令是否仍有效
     *
     * @param instruction 導向指令
     * @return 是否仍有效
     */
    fun isInstructionValid(instruction: RedirectInstruction): Boolean
}

/**
 * 驗簽 / 安全能力（強烈建議實作）
 *
 * 提供資料完整性和真實性驗證：
 * - Callback 驗簽
 * - API 回應驗簽
 * - 防篡改驗證
 *
 * 特點：
 * - 無狀態操作
 * - 純演算法能力
 * - 可獨立測試和審計
 */
interface SignatureCapability {

    /**
     * 驗證簽名
     *
     * @param payload 原始數據
     * @param signature 簽名
     * @param algorithm 演算法（如果支援多種）
     * @return 驗證結果
     */
    fun verifySignature(
        payload: String,
        signature: String,
        algorithm: String? = null
    ): SignatureVerificationResult

    /**
     * 產生簽名（用於發送請求）
     *
     * @param payload 要簽名的數據
     * @param algorithm 演算法
     * @return 簽名結果
     */
    fun generateSignature(
        payload: String,
        algorithm: String? = null
    ): String

    /**
     * 取得支援的簽名演算法
     *
     * @return 支援的演算法清單
     */
    fun getSupportedAlgorithms(): List<String>
}

/**
 * 簽名驗證結果
 */
data class SignatureVerificationResult(
    /**
     * 驗證是否成功
     */
    val isValid: Boolean,

    /**
     * 使用的演算法
     */
    val algorithm: String,

    /**
     * 錯誤訊息（如果驗證失敗）
     */
    val errorMessage: String? = null,

    /**
     * 額外資訊
     */
    val metadata: Map<String, String> = emptyMap()
)

/**
 * 對帳 / 清算能力（進階功能）
 *
 * 用於：
 * - 批次對帳
 * - 財務清算
 * - 數據一致性檢查
 *
 * 注意：這是進階功能，多數系統初期不需要
 */
interface ReconcileCapability {

    /**
     * 執行對帳
     *
     * @param request 對帳請求
     * @return 對帳結果
     */
    fun reconcile(request: ReconcileRequest): ReconcileResult

    /**
     * 取得可對帳的日期範圍
     *
     * @return 可對帳的時間範圍
     */
    fun getReconcilableDateRange(): DateRange

    /**
     * 下載對帳檔案
     *
     * @param request 對帳請求
     * @return 檔案內容或下載連結
     */
    fun downloadReconcileFile(request: ReconcileRequest): ReconcileFile
}

/**
 * 對帳檔案
 */
data class ReconcileFile(
    /**
     * 檔案名稱
     */
    val fileName: String,

    /**
     * 檔案類型
     */
    val fileType: String,

    /**
     * 檔案內容（小檔案直接回傳）
     */
    val content: ByteArray? = null,

    /**
     * 下載連結（大檔案提供連結）
     */
    val downloadUrl: String? = null,

    /**
     * 檔案大小
     */
    val fileSize: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReconcileFile

        if (fileName != other.fileName) return false
        if (fileType != other.fileType) return false
        if (content != null) {
            if (other.content == null) return false
            if (!content.contentEquals(other.content)) return false
        } else if (other.content != null) return false
        if (downloadUrl != other.downloadUrl) return false
        if (fileSize != other.fileSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + fileType.hashCode()
        result = 31 * result + (content?.contentHashCode() ?: 0)
        result = 31 * result + (downloadUrl?.hashCode() ?: 0)
        result = 31 * result + (fileSize?.hashCode() ?: 0)
        return result
    }
}