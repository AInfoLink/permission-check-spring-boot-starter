package com.module.app.core.service

import com.module.app.core.tenant.service.OrderItemCategoryConfigLoader
import com.module.app.core.models.CategoryType
import com.module.app.core.models.OrderItemCategory
import com.module.app.core.models.OperationType
import com.module.app.core.repository.OrderItemCategoryRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 專門負責 ItemCategory 初始化的服務
 * 職責單一：系統啟動時初始化預設分類
 */
@Service
class OrderItemCategoryInitializationService(
    private val categoryRepository: OrderItemCategoryRepository,
    private val configLoader: OrderItemCategoryConfigLoader
) {

    /**
     * 系統啟動時初始化預設分類
     */
    @Transactional
    @PostConstruct
    fun init() {
        // 確保 categoryConfig 已經載入完成
//        categoryConfig.loadConfig()
//        initializeSystemManagedCategories()
    }

    /**
     * 初始化系統預設分類
     */
    @Transactional
    fun initializeSystemManagedCategories() {
        configLoader.systemManagedCategories.forEach { config ->
            if (!categoryRepository.existsByCode(config.code)) {
                val category = OrderItemCategory(
                    code = config.code,
                    name = config.name,
                    description = config.description,
                    type = CategoryType.SYSTEM_MANAGED,
                    operationType = OperationType.valueOf(config.operationType),
                    isActive = true,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                categoryRepository.save(category)
            }
        }
    }
}