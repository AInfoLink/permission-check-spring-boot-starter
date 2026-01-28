# Permission Annotations Skill

## 目的
為 Kotlin 多租戶預訂服務系統自動添加和管理權限註解，確保所有服務層方法都有適當的權限控制。

## 核心功能

### 1. 為新服務添加權限註解
- 自動為 Service 類添加 `@Domain` 註解
- 為所有 public 方法添加適當的 `@Require` 註解
- 根據方法名稱和操作類型自動推斷權限

### 2. 擴展權限系統
- 在 `Permission` enum 中添加新權限
- 在 `DomainType` enum 中添加新領域
- 維護權限命名一致性

### 3. 權限審核和驗證
- 檢查現有服務的權限註解完整性
- 驗證權限命名規範
- 生成權限使用報告

## 權限設計規範

### Domain 分類
```kotlin
enum class DomainType {
    USERS,      // 用戶管理
    ORDERS,     // 訂單和訂單項目管理
    VENUES,     // 場館和場館組管理
    BOOKINGS,   // 預約管理
    WALLETS,    // 錢包管理
    SYSTEM      // 系統管理
}
```

### 權限命名規範
格式: `{DOMAIN}_{ACTION}[_{SCOPE}]`

**基本操作:**
- `READ` - 查詢操作
- `CREATE` - 創建操作
- `UPDATE` - 更新操作
- `DELETE` - 刪除操作

**作用範圍:**
- 無後綴 - 全域權限 (如 `USERS_READ`)
- `_OWN` - 僅自己的資源 (如 `USERS_READ_OWN`)
- `_ALL` - 領域全權限 (如 `USERS_ALL`)

### Service 註解模式
```kotlin
@Service
@Domain(DomainType.ORDERS)
class DefaultOrderService(...): OrderService {

    @Require(Permission.ORDERS_CREATE)
    @Transactional
    override fun createOrder(order: OrderCreation): Order { ... }

    @Require(Permission.ORDERS_READ)
    @Transactional(readOnly = true)
    override fun getOrderById(orderId: UUID): Order { ... }
}
```

## 使用方法

### 為新 Service 添加權限註解
```
/permission-annotations add-service <ServiceName> <DomainType>
```

### 為現有方法添加權限
```
/permission-annotations add-method <ClassName> <MethodName> <Permission>
```

### 創建新權限
```
/permission-annotations add-permission <PermissionName> <Description>
```

### 審核權限配置
```
/permission-annotations audit [domain]
```

## 自動推斷規則

### 方法名稱 → 權限映射
- `create*`, `add*`, `save*` → `{DOMAIN}_CREATE`
- `get*`, `find*`, `list*`, `search*` → `{DOMAIN}_READ`
- `update*`, `modify*`, `edit*` → `{DOMAIN}_UPDATE`
- `delete*`, `remove*` → `{DOMAIN}_DELETE`

### 特殊權限
- 錢包相關操作 → `WALLETS_*` 權限
- 批量操作 → 對應的單一操作權限
- 驗證方法 → `{DOMAIN}_READ` 權限

## 輸出格式

### 添加權限註解後的報告
```
✅ 權限註解添加完成

📊 統計信息:
- 處理的服務: 5 個
- 添加的方法註解: 23 個
- 新增的權限: 3 個

🔍 權限概覽:
OrderService (ORDERS domain):
  - createOrder() → ORDERS_CREATE
  - getOrderById() → ORDERS_READ
  - updateOrder() → ORDERS_UPDATE

⚠️ 注意事項:
- 請確認 bulkUpdateOrderItems 的權限設定
- 建議為敏感操作添加額外驗證
```

## 文件結構

### 權限註解文件位置
```
src/main/kotlin/com/module/multitenantbookingservice/security/annotation/
├── Domain.kt              # @Domain 註解
├── Require.kt             # @Require 註解
├── DomainType.kt          # Domain 枚舉
└── Permission.kt          # Permission 枚舉
```

### 支援的 Service 模式
- Spring `@Service` 類
- 實現接口的服務類
- `@Transactional` 方法
- Repository 模式 (可選)

## 最佳實踐

### 1. 權限層次設計
```kotlin
// 客戶權限 (最小權限)
CUSTOMER_PERMISSIONS = setOf(
    Permission.ORDERS_READ_OWN,
    Permission.ORDERS_CREATE_OWN,
    Permission.USERS_READ_OWN,
    Permission.WALLETS_READ_OWN
)

// 員工權限 (部分全域權限)
STAFF_PERMISSIONS = CUSTOMER_PERMISSIONS + setOf(
    Permission.ORDERS_READ,
    Permission.ORDERS_UPDATE,
    Permission.VENUES_READ,
    Permission.WALLETS_RECHARGE
)

// 管理員權限 (領域全權限)
MANAGER_PERMISSIONS = setOf(
    Permission.ORDERS_ALL,
    Permission.VENUES_ALL,
    Permission.USERS_ALL,
    Permission.WALLETS_ALL
)
```

### 2. 資源擁有者檢查
對於 `*_OWN` 權限，配合 `HasResourceOwner` 接口使用:
```kotlin
interface HasResourceOwner {
    fun getResourceOwnerId(): String
}
```

### 3. 權限檢查時機
- **Controller 層**: 基礎角色檢查
- **Service 層**: 詳細權限邏輯 (主要使用此 skill)
- **Repository 層**: 資源過濾

## 錯誤處理

### 常見問題
1. **缺少權限註解**: 自動檢測並提示添加
2. **權限命名不規範**: 提供重命名建議
3. **循環依賴**: 檢查服務間的權限依賴關係
4. **權限過度授予**: 警告可能的安全問題

### 驗證規則
- 所有 public service 方法必須有 `@Require` 註解
- 權限名稱必須符合命名規範
- Domain 歸屬必須合理 (如 ItemCategory → ORDERS)
- 避免使用 `SYSTEM_ALL` 權限 (除非必要)