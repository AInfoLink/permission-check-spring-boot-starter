package com.module.multitenantbookingservice.core.service

import com.module.multitenantbookingservice.core.models.CategoryType
import com.module.multitenantbookingservice.core.models.ItemCategory
import com.module.multitenantbookingservice.core.repository.ItemCategoryRepository
import com.module.multitenantbookingservice.security.ItemCategoryAlreadyExists
import com.module.multitenantbookingservice.security.ItemCategoryNotFound
import com.module.multitenantbookingservice.security.SystemManagedCategoryDeletionDenied
import com.module.multitenantbookingservice.security.SystemManagedCategoryModificationDenied
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ItemCategoryService(
    private val categoryRepository: ItemCategoryRepository
) {

    data class SystemCategoryConfig(
        val code: String,
        val name: String,
        val description: String,
    )

    companion object {
        // AWS-style: 系統預設的 Managed Categories
        private val SYSTEM_MANAGED_CATEGORIES = listOf(
            SystemCategoryConfig("BOOKING", "預約", "場地預約、活動預約等"),
            SystemCategoryConfig("RENTAL", "租借", "設備租借、器材租借等"),
            SystemCategoryConfig("SERVICE", "服務", "各類服務費用"),
            SystemCategoryConfig("MEMBERSHIP", "會員", "會員費、會員相關費用"),
            SystemCategoryConfig("TOPUP", "儲值", "錢包儲值、點數購買等"),
            SystemCategoryConfig("MERCHANDISE", "商品", "實體商品販售"),
            SystemCategoryConfig("FEE", "費用", "手續費、服務費等"),
            SystemCategoryConfig("DISCOUNT", "折扣", "各類折扣優惠"),
            SystemCategoryConfig("REFUND", "退款", "退費、退款等"),
            SystemCategoryConfig("OTHER", "其他", "其他未分類項目")
        )
    }

    /**
     * 系統啟動時初始化預設分類
     */
    @Transactional
    @PostConstruct
    fun init(vararg args: String?) {
        initializeSystemManagedCategories()
    }

    @Transactional
    fun initializeSystemManagedCategories() {
        SYSTEM_MANAGED_CATEGORIES.forEach { config ->
            if (!categoryRepository.existsByCode(config.code)) {
                val category = ItemCategory(
                    code = config.code,
                    name = config.name,
                    description = config.description,
                    type = CategoryType.SYSTEM_MANAGED,
                    isActive = true,
                    createdAt = java.time.Instant.now(),
                    updatedAt = java.time.Instant.now()
                )
                categoryRepository.save(category)
            }
        }
    }

    /**
     * 取得所有活躍分類（系統+用戶）
     */
    fun getAllActiveCategories(): List<ItemCategory> {
        return categoryRepository.findByIsActiveTrueOrderBySortOrder()
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
    ): ItemCategory {
        if (categoryRepository.existsByCode(code)) {
            throw ItemCategoryAlreadyExists.withDetails("Category code '$code' already exists")
        }

        val category = ItemCategory(
            code = code,
            name = name,
            description = description,
            type = CategoryType.USER_MANAGED,
            isActive = true,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
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
        sortOrder: Int? = null,
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