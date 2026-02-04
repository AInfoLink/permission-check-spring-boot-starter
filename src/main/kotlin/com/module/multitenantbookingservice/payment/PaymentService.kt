package com.module.multitenantbookingservice.payment

import com.module.multitenantbookingservice.payment.model.*

enum class PaymentServiceType(name: String) {
    LINE("line"),
    EC("ec"),
    JKO("jko"),
    WALLET("wallet")
}


/**
 * 核心付款服務介面
 *
 * 這是付款流程的唯一入口，所有金流實作都必須實現此介面
 *
 * 設計原則：
 * - 流程不可被 mixin 分散
 * - 狀態轉移只能存在一個地方：initiate → callback → confirmed/failed
 * - 上層代碼只依賴此介面
 */
interface PaymentService<T> {

    fun getPaymentConfig(): T
    /**
     * 啟動付款流程（不保證付款完成）
     *
     * 這個方法只是向金流系統發起付款請求，不代表付款完成。
     * 付款是否成功需要透過 handlePaymentCallback 來確認。
     *
     * @param request 付款請求資料
     * @return 付款初始化結果，包含金流系統回傳的相關資訊
     */
    fun initiatePayment(request: PaymentRequest): PaymentInitiationResult

    /**
     * 處理金流系統回調（callback / webhook）
     *
     * 這裡才是「付款是否成功」的最終依據。
     * 所有金流系統都會透過回調來通知付款結果。
     *
     * @param callback 金流系統的回調資料
     * @return 付款最終結果
     */
    fun handlePaymentCallback(callback: PaymentCallback, hookAction: (() -> Unit)? = null): PaymentResult

    /**
     * 取得付款服務的識別名稱
     *
     * @return 付款服務名稱（如：linepay, ecpay, jkopay）
     */
    fun getServiceType(): PaymentServiceType
}