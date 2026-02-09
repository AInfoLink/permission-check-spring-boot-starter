# Permission Check Spring Boot Starter

A domain-agnostic Spring Boot starter for annotation-based permission checking with AOP.

## Quick Start

### 1. Add Dependency

```kotlin
dependencies {
    implementation("io.github.common:permission-check-spring-boot-starter:1.0.0-SNAPSHOT")
}
```

### 2. Enable Permission Checking

```kotlin
@SpringBootApplication
@EnablePermissionCheck
class MyApplication
```

### 3. Use @Require Annotation

```kotlin
@Service
class OrderService {

    @Require("orders:read")
    fun getOrders(): List<Order> { ... }

    @Require("orders:create")
    fun createOrder(order: Order): Order { ... }

    @Require(permissions = ["orders:read", "users:read"], requireAll = true)
    fun getOrdersWithUsers(): OrderUserData { ... }
}
```

**That's it!** The starter works out-of-the-box with Spring Security roles.

## Default Behavior

The starter provides intelligent defaults that work with standard Spring Security setups:

### Role to Permission Mapping

- `ROLE_ADMIN` → `*:*` (system admin, all permissions)
- `ROLE_MANAGER` → Extended permissions (users:*, orders:*, bookings:*)
- `ROLE_USER` → Basic read permissions (users:read, orders:read, venues:read, bookings:read)
- Custom authorities following `domain:action` format are used as-is
- Other roles like `ROLE_CUSTOM` → `custom:*`

### User ID Extraction

The starter automatically extracts user IDs from common Spring Security principal patterns:
- Any object with `getId()` method returning UUID or String
- Objects with `id` property
- String principals that can be parsed as UUID

## Advanced Configuration

### Three Levels of Customization

#### Level 1: PermissionAware Interface (Recommended for simple cases)

```kotlin
@Entity
class User : UserDetails, PermissionAware {
    // ... other properties

    override fun getPermissions(): Set<String> {
        return roles.flatMap { it.permissions }.toSet()
    }
}
```

#### Level 2: Custom Permission Repository (Most common)

```kotlin
@Component
@Primary
class ApplicationPermissionRepository(
    private val userProfileService: UserProfileService
) : PermissionRepository {
    override fun getUserPermissions(userId: UUID): Set<String> {
        val userProfile = userProfileService.getUserProfileInternal(userId)
        return userProfile.tenantRoles.flatMap { it.permissions }.toSet()
    }
}
```

### Custom User Provider

For non-standard user models:

```kotlin
@Component
@Primary
class CustomCurrentUserProvider : CurrentUserProvider {
    override fun getCurrentUserId(): UUID? {
        val auth = SecurityContextHolder.getContext().authentication
        val customUser = auth?.principal as? CustomUserType
        return customUser?.userId
    }
}
```

### Configuration Properties

```yaml
permission-check:
  cache:
    enabled: true
    cache-name: "userPermissions"
    ttl-seconds: 300
  logging:
    debug-enabled: false
    audit-enabled: true
```

## Permission String Format

The starter supports flexible permission string formats:

```kotlin
// Basic format
@Require("orders:read")
@Require("users:create")

// Wildcard permissions
@Require("orders:*")    // All order actions
@Require("*:read")      // Read any resource
@Require("*:*")         // System admin

// Multiple permissions
@Require(permissions = ["orders:read", "users:read"], requireAll = true)   // AND
@Require(permissions = ["admin:*", "manager:*"], requireAll = false)       // OR

// Hierarchical permissions
@Require("system:admin:full")
@Require("tenant:manage:users")
```

## Exception Handling

```kotlin
@ControllerAdvice
class PermissionExceptionHandler {

    @ExceptionHandler(PermissionDeniedException::class)
    fun handlePermissionDenied(e: PermissionDeniedException): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to "Access denied", "details" to e.message))
    }

    @ExceptionHandler(SecurityException::class)
    fun handleNotAuthenticated(e: SecurityException): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("error" to "Authentication required"))
    }
}
```

## Migration from Custom Permission Systems

If you have an existing permission system:

1. Add `@EnablePermissionCheck` to your main class
2. Update `@Require` annotations to use strings instead of enums:
   ```kotlin
   // Before
   @Require(Permission.ORDERS_READ)

   // After
   @Require("orders:read")
   ```
3. Implement `PermissionRepository` to bridge your data model
4. Optionally implement `CurrentUserProvider` for custom user models

## Examples

### E-commerce Application

```kotlin
@Service
class ProductService {
    @Require("products:read")
    fun getProducts(): List<Product> { ... }

    @Require("products:create")
    fun createProduct(product: Product): Product { ... }

    @Require(permissions = ["products:read", "inventory:read"], requireAll = true)
    fun getProductsWithInventory(): List<ProductInventory> { ... }
}
```

### Multi-tenant SaaS

```kotlin
@Service
class TenantService {
    @Require("tenants:read")
    fun getTenants(): List<Tenant> { ... }

    @Require("tenants:admin")
    fun createTenant(tenant: Tenant): Tenant { ... }

    @Require(permissions = ["billing:read", "tenants:read"], requireAll = true)
    fun getTenantBilling(tenantId: UUID): BillingInfo { ... }
}
```

## Benefits

- **Zero Configuration**: Works out-of-the-box with Spring Security
- **Domain Agnostic**: Use any permission string format
- **Flexible**: Override defaults when needed
- **Performance**: Built-in caching with Spring Cache abstraction
- **AOP-Based**: Clean separation of business and security logic
- **Type-Safe**: Compile-time checking of permission strings
- **Auditable**: Comprehensive logging for security compliance