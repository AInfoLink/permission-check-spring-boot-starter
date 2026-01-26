package com.module.multitenantbookingservice.core.service

import com.module.multitenantbookingservice.core.config.ItemCategoryConfig
import com.module.multitenantbookingservice.core.models.CategoryType
import com.module.multitenantbookingservice.core.models.ItemCategory
import com.module.multitenantbookingservice.core.models.OperationType
import com.module.multitenantbookingservice.core.repository.ItemCategoryRepository
import com.module.multitenantbookingservice.security.ItemCategoryAlreadyExists
import com.module.multitenantbookingservice.security.ItemCategoryNotFound
import com.module.multitenantbookingservice.security.SystemManagedCategoryDeletionDenied
import com.module.multitenantbookingservice.security.SystemManagedCategoryModificationDenied
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class ItemCategoryService(
    private val categoryRepository: ItemCategoryRepository,
    private val categoryConfig: ItemCategoryConfig
) {

    /**
     * 系統啟動時初始化預設分類
     */
    @Transactional
    @PostConstruct
    fun init() {
        // 確保 categoryConfig 已經載入完成
        categoryConfig.loadConfig()
//        initializeSystemManagedCategories()
    }

    @Transactional
    fun initializeSystemManagedCategories() {
        categoryConfig.systemManagedCategories.forEach { config ->
            if (!categoryRepository.existsByCode(config.code)) {
                return@forEach
            }
            val category = ItemCategory(
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

    /**
     * 取得所有活躍分類（系統+用戶）
     */
    fun getAllActiveCategories(): List<ItemCategory> {
        return categoryRepository.findByIsActiveTrue()
    }

    /**
     * 取得系統預設分類
     */
    fun getSystemManagedCategories(): List<ItemCategory> {
        return categoryRepository.findSystemManagedCategories()
    }

    /**
     * 取得用戶自訂分類
     */
    fun getUserManagedCategories(): List<ItemCategory> {
        return categoryRepository.findUserManagedCategories()
    }

    /**
     * 建立用戶自訂分類
     */
    @Transactional
    fun createUserCategory(
        code: String,
        name: String,
        description: String? = null,
        operationType: OperationType = OperationType.CHARGE
    ): ItemCategory {
        if (categoryRepository.existsByCode(code)) {
            throw ItemCategoryAlreadyExists.withDetails("Category code '$code' already exists")
        }

        val category = ItemCategory(
            code = code,
            name = name,
            description = description,
            type = CategoryType.USER_MANAGED,
            operationType = operationType,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        return categoryRepository.save(category)
    }

    /**
     * 更新用戶自訂分類（系統分類不可修改）
     */
    @Transactional
    fun updateUserCategory(
        id: UUID,
        name: String? = null,
        description: String? = null,
        isActive: Boolean? = null
    ): ItemCategory {
        val category = categoryRepository.findById(id)
            .orElseThrow { ItemCategoryNotFound.withDetails("Category with id '$id' not found") }

        if (category.type == CategoryType.SYSTEM_MANAGED) {
            throw SystemManagedCategoryModificationDenied.withDetails("System managed category cannot be modified")
        }

        name?.let { category.name = it }
        description?.let { category.description = it }
        isActive?.let { category.isActive = it }

        return categoryRepository.save(category)
    }

    /**
     * 根據 code 取得分類
     */
    fun getCategoryByCode(code: String): ItemCategory? {
        return categoryRepository.findByCode(code)
    }

    /**
     * 刪除用戶自訂分類（系統分類不可刪除）
     */
    @Transactional
    fun deleteUserCategory(id: UUID) {
        val category = categoryRepository.findById(id)
            .orElseThrow { ItemCategoryNotFound.withDetails("Category with id '$id' not found") }

        if (category.type == CategoryType.SYSTEM_MANAGED) {
            throw SystemManagedCategoryDeletionDenied.withDetails("System managed category cannot be deleted")
        }

        categoryRepository.delete(category)
    }
}