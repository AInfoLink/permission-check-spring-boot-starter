package com.module.app.core.web.admin

import com.module.app.core.models.OrderItemCategory
import com.module.app.core.models.OperationType
import com.module.app.core.service.ItemCategoryCreation
import com.module.app.core.service.OrderItemCategoryService
import com.module.app.core.service.ItemCategoryUpdate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/admin/categories")
class OrderItemCategoryController(
    private val categoryService: OrderItemCategoryService
) {

    /**
     * 取得所有活躍分類
     */
    @GetMapping
    fun getAllActiveCategories(): ResponseEntity<List<OrderItemCategory>> {
        val categories = categoryService.getAllActiveCategories()
        return ResponseEntity.ok(categories)
    }

    /**
     * 取得系統預設分類
     */
    @GetMapping("/system")
    fun getSystemManagedCategories(): ResponseEntity<List<OrderItemCategory>> {
        val categories = categoryService.getSystemManagedCategories()
        return ResponseEntity.ok(categories)
    }

    /**
     * 取得用戶自訂分類
     */
    @GetMapping("/user")
    fun getUserManagedCategories(): ResponseEntity<List<OrderItemCategory>> {
        val categories = categoryService.getUserManagedCategories()
        return ResponseEntity.ok(categories)
    }

    /**
     * 根據 code 取得分類
     */
    @GetMapping("/code/{code}")
    fun getCategoryByCode(@PathVariable code: String): ResponseEntity<OrderItemCategory?> {
        val category = categoryService.getCategoryByCode(code)
        return if (category != null) {
            ResponseEntity.ok(category)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 建立用戶自訂分類
     */
    @PostMapping
    fun createUserCategory(@RequestBody request: ItemCategoryCreation): ResponseEntity<OrderItemCategory> {
        val category = categoryService.createUserCategory(request)
        return ResponseEntity(category, HttpStatus.CREATED)
    }

    /**
     * 更新用戶自訂分類
     */
    @PutMapping("/{categoryId}")
    fun updateUserCategory(
        @PathVariable categoryId: UUID,
        @RequestBody update: ItemCategoryUpdate
    ): ResponseEntity<OrderItemCategory> {
        val category = categoryService.updateUserCategory(categoryId, update)
        return ResponseEntity.ok(category)
    }

    /**
     * 刪除用戶自訂分類
     */
    @DeleteMapping("/{categoryId}")
    fun deleteUserCategory(@PathVariable categoryId: UUID): ResponseEntity<Map<String, String>> {
        categoryService.deleteUserCategory(categoryId)
        return ResponseEntity.ok(mapOf("message" to "Category deleted successfully"))
    }

    /**
     * 取得所有操作類型
     */
    @GetMapping("/operation-types")
    fun getOperationTypes(): ResponseEntity<Map<String, Any>> {
        val operationTypes = OperationType.entries.map { type ->
            mapOf(
                "name" to type.name,
                "description" to when(type) {
                    OperationType.CHARGE -> "收費操作：正值金額，如預訂、租借"
                    OperationType.REFUND -> "退費操作：負值金額，如取消、退款"
                    OperationType.NEUTRAL -> "中性操作：無金額變動，如查詢、確認"
                }
            )
        }
        return ResponseEntity.ok(mapOf("operationTypes" to operationTypes))
    }
}