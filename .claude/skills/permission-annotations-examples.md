# Permission Annotations - 使用範例

## 快速開始

### 1. 為新 Service 添加權限註解
假設你有一個新的 `BookingService`:

**之前:**
```kotlin
@Service
class DefaultBookingService(
    private val bookingRepository: BookingRepository
): BookingService {

    override fun createBooking(request: BookingRequest): Booking {
        // 實現邏輯
    }

    override fun getBooking(id: UUID): Booking {
        // 實現邏輯
    }
}
```

**使用 skill 後:**
```kotlin
@Service
@Domain(DomainType.BOOKINGS)
class DefaultBookingService(
    private val bookingRepository: BookingRepository
): BookingService {

    @Require(Permission.BOOKINGS_CREATE)
    @Transactional
    override fun createBooking(request: BookingRequest): Booking {
        // 實現邏輯
    }

    @Require(Permission.BOOKINGS_READ)
    @Transactional(readOnly = true)
    override fun getBooking(id: UUID): Booking {
        // 實現邏輯
    }
}
```

### 2. 權限 Enum 自動擴展
同時會在 `Permission.kt` 中自動添加:
```kotlin
enum class Permission(val value: String) {
    // ... 現有權限

    // 預約管理 (Bookings) - 新增
    BOOKINGS_READ("bookings:read"),
    BOOKINGS_READ_OWN("bookings:read:own"),
    BOOKINGS_CREATE("bookings:create"),
    BOOKINGS_CREATE_OWN("bookings:create:own"),
    BOOKINGS_UPDATE("bookings:update"),
    BOOKINGS_UPDATE_OWN("bookings:update:own"),
    BOOKINGS_CANCEL("bookings:cancel"),
    BOOKINGS_CANCEL_OWN("bookings:cancel:own"),
    BOOKINGS_ALL("bookings:*"),
}
```

## 常用命令範例

### 添加整個服務的權限註解
```bash
請為 BookingService 添加完整的權限註解，領域為 BOOKINGS
```

### 為現有方法添加權限
```bash
請為 UserProfileService.updateWalletBalance 方法添加 WALLETS_ADJUST 權限註解
```

### 審核現有權限
```bash
請檢查 OrderService 的權限註解完整性，並生成報告
```

### 創建新權限
```bash
請在 Permission enum 中添加 REPORTS_EXPORT 權限，用於報表匯出功能
```

## 實際案例

### 案例 1: 會員服務權限設計
```kotlin
@Service
@Domain(DomainType.USERS)
class MembershipService(
    private val membershipRepository: MembershipRepository
): MembershipService {

    @Require(Permission.USERS_READ)
    override fun getMembership(userId: UUID): Membership? { ... }

    @Require(Permission.USERS_UPDATE)
    override fun upgradeMembership(userId: UUID, level: MembershipLevel): Membership { ... }

    @Require(Permission.USERS_UPDATE)
    override fun addMembershipPoints(userId: UUID, points: Int): Membership { ... }

    @Require(Permission.WALLETS_ADJUST)  // 特殊：錢包相關
    override fun applyMembershipDiscount(userId: UUID, amount: BigDecimal): BigDecimal { ... }
}
```

### 案例 2: 複雜權限場景
```kotlin
@Service
@Domain(DomainType.BOOKINGS)
class BookingService {

    // 一般用戶只能查看自己的預約，管理員可以查看所有
    @Require(Permission.BOOKINGS_READ)  // AOP會處理 :own 邏輯
    override fun getBookingsByUser(userId: UUID): List<Booking> { ... }

    // 創建預約：一般用戶只能為自己創建，櫃台人員可為他人創建
    @Require(Permission.BOOKINGS_CREATE)  // AOP會處理 :own 邏輯
    override fun createBooking(request: BookingRequest): Booking { ... }

    // 取消預約：特殊權限
    @Require(Permission.BOOKINGS_CANCEL)
    override fun cancelBooking(bookingId: UUID, reason: String): Booking { ... }
}
```

## 權限檢查邏輯範例

### AOP 權限檢查器
```kotlin
@Aspect
@Component
class PermissionAspect {

    @Around("@annotation(require)")
    fun checkPermission(joinPoint: ProceedingJoinPoint, require: Require): Any? {
        val permission = require.permission.value
        val currentUser = getCurrentUser()
        val resourceOwnerId = extractResourceOwnerId(joinPoint.args)

        if (!hasPermission(currentUser, permission, resourceOwnerId)) {
            throw AccessDeniedException("權限不足: ${permission}")
        }

        return joinPoint.proceed()
    }

    private fun hasPermission(user: User, permission: String, resourceOwnerId: String?): Boolean {
        // 1. 系統全權限
        if (user.hasPermission("*:*")) return true

        // 2. 領域全權限
        val domain = permission.split(":")[0]
        if (user.hasPermission("${domain}:*")) return true

        // 3. 具體全域權限
        if (user.hasPermission(permission)) return true

        // 4. :own 權限檢查
        val ownPermission = "${permission}:own"
        if (user.hasPermission(ownPermission) && user.id.toString() == resourceOwnerId) {
            return true
        }

        return false
    }
}
```

## 權限配置範例

### 角色權限對應
```kotlin
object RolePermissions {
    val CUSTOMER = setOf(
        Permission.USERS_READ_OWN,
        Permission.USERS_UPDATE_OWN,
        Permission.BOOKINGS_READ_OWN,
        Permission.BOOKINGS_CREATE_OWN,
        Permission.BOOKINGS_CANCEL_OWN,
        Permission.ORDERS_READ_OWN,
        Permission.WALLETS_READ_OWN,
        Permission.WALLETS_RECHARGE_OWN
    )

    val STAFF = CUSTOMER + setOf(
        Permission.USERS_READ,
        Permission.BOOKINGS_READ,
        Permission.BOOKINGS_CREATE,
        Permission.VENUES_READ,
        Permission.WALLETS_RECHARGE
    )

    val MANAGER = setOf(
        Permission.USERS_ALL,
        Permission.BOOKINGS_ALL,
        Permission.ORDERS_ALL,
        Permission.VENUES_ALL,
        Permission.WALLETS_ALL
    )

    val SUPER_ADMIN = setOf(Permission.SYSTEM_ALL)
}
```

## 提示和技巧

### 1. 方法命名與權限對應
- `findBy*`, `getBy*`, `listAll*` → `{DOMAIN}_READ`
- `create*`, `save*`, `add*` → `{DOMAIN}_CREATE`
- `update*`, `modify*`, `patch*` → `{DOMAIN}_UPDATE`
- `delete*`, `remove*`, `cancel*` → `{DOMAIN}_DELETE`

### 2. 特殊情況處理
```kotlin
// 錢包操作，即使在 UserService 中也用 WALLETS_ 權限
@Require(Permission.WALLETS_ADJUST)
override fun updateWalletBalance(...) { ... }

// 批量操作使用對應的單一操作權限
@Require(Permission.ORDER_ITEMS_UPDATE)
override fun bulkUpdateOrderItems(...) { ... }

// 驗證類方法使用 READ 權限
@Require(Permission.ORDER_ITEMS_READ)
override fun validateOrderItemAmount(...) { ... }
```

### 3. 避免的反模式
```kotlin
// ❌ 不要：權限過於寬泛
@Require(Permission.SYSTEM_ALL)
override fun getUser(id: UUID): User { ... }

// ✅ 應該：使用具體權限
@Require(Permission.USERS_READ)
override fun getUser(id: UUID): User { ... }

// ❌ 不要：硬編碼字串
@Require(action = "users:read")  // 舊版方式

// ✅ 應該：使用 enum
@Require(Permission.USERS_READ)
```