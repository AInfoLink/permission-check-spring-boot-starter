package com.module.app.core.service
import com.module.app.core.models.CategoryType
import com.module.app.core.models.OrderItemCategory
import com.module.app.core.models.OperationType
import com.module.app.core.repository.OrderItemCategoryRepository
import com.module.app.security.ItemCategoryAlreadyExists
import com.module.app.security.ItemCategoryNotFound
import com.module.app.security.SystemManagedCategoryDeletionDenied
import com.module.app.security.SystemManagedCategoryModificationDenied
import com.module.app.security.annotation.Permission
import com.module.app.security.annotation.Require
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

// DTO classes for ItemCategory operations
data class ItemCategoryCreation(
    val code: String,
    val name: String,
    val description: String? = null,
    val operationType: OperationType = OperationType.CHARGE
)

data class ItemCategoryUpdate(
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)

interface OrderItemCategoryService {
    fun getAllActiveCategories(): List<OrderItemCategory>
    fun getSystemManagedCategories(): List<OrderItemCategory>
    fun getUserManagedCategories(): List<OrderItemCategory>
    fun createUserCategory(request: ItemCategoryCreation): OrderItemCategory
    fun updateUserCategory(id: UUID, update: ItemCategoryUpdate): OrderItemCategory
    fun getCategoryByCode(code: String): OrderItemCategory?
    fun deleteUserCategory(id: UUID)
}

@Service
class DefaultOrderItemCategoryService(
    private val categoryRepository: OrderItemCategoryRepository
) : OrderItemCategoryService {


    /**
     * 取得所有活躍分類（系統+用戶）
     */
    @Require(Permission.CATEGORIES_READ)
    override fun getAllActiveCategories(): List<OrderItemCategory> {
        return categoryRepository.findByIsActiveTrue()
    }

    /**
     * 取得系統預設分類
     */
    @Require(Permission.CATEGORIES_READ)
    override fun getSystemManagedCategories(): List<OrderItemCategory> {
        return categoryRepository.findSystemManagedCategories()
    }

    /**
     * 取得用戶自訂分類
     */
    @Require(Permission.CATEGORIES_READ)
    override fun getUserManagedCategories(): List<OrderItemCategory> {
        return categoryRepository.findUserManagedCategories()
    }

    /**
     * 建立用戶自訂分類
     */
    @Require(Permission.CATEGORIES_CREATE)
    @Transactional
    override fun createUserCategory(request: ItemCategoryCreation): OrderItemCategory {
        if (categoryRepository.existsByCode(request.code)) {
            throw ItemCategoryAlreadyExists.withDetails("Category code '${request.code}' already exists")
        }

        val category = OrderItemCategory(
            code = request.code,
            name = request.name,
            description = request.description,
            type = CategoryType.USER_MANAGED,
            operationType = request.operationType,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        return categoryRepository.save(category)
    }

    /**
     * 更新用戶自訂分類（系統分類不可修改）
     */
    @Require(Permission.CATEGORIES_UPDATE)
    @Transactional
    override fun updateUserCategory(id: UUID, update: ItemCategoryUpdate): OrderItemCategory {
        val category = categoryRepository.findById(id)
            .orElseThrow { ItemCategoryNotFound.withDetails("Category with id '$id' not found") }

        if (category.type == CategoryType.SYSTEM_MANAGED) {
            throw SystemManagedCategoryModificationDenied.withDetails("System managed category cannot be modified")
        }

        update.name?.let { category.name = it }
        update.description?.let { category.description = it }
        update.isActive?.let { category.isActive = it }

        return categoryRepository.save(category)
    }

    /**
     * 根據 code 取得分類
     */
    @Require(Permission.CATEGORIES_READ)
    override fun getCategoryByCode(code: String): OrderItemCategory? {
        return categoryRepository.findByCode(code)
    }

    /**
     * 刪除用戶自訂分類（系統分類不可刪除）
     */
    @Require(Permission.CATEGORIES_DELETE)
    @Transactional
    override fun deleteUserCategory(id: UUID) {
        val category = categoryRepository.findById(id)
            .orElseThrow { ItemCategoryNotFound.withDetails("Category with id '$id' not found") }

        if (category.type == CategoryType.SYSTEM_MANAGED) {
            throw SystemManagedCategoryDeletionDenied.withDetails("System managed category cannot be deleted")
        }

        categoryRepository.delete(category)
    }
}