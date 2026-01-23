package com.module.multitenantbookingservice.core.models

import com.app.security.repository.model.User
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*


@Entity
@Table(
    name = "user_profiles",
    indexes = [
        Index(name = "idx_user_profiles_user_id", columnList = "user_id"),
        Index(name = "idx_user_profiles_active", columnList = "is_active")
    ]
)
class UserProfile(
    @Id
    val id: UUID = UUID.randomUUID(),

    /**
     * Reference to global user identity in public schema.
     * 使用 cross schema foreign key 確保參照完整性
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_user_profiles_user_id")
    )
    val user: User,

    /**
     * 租戶特定的角色 (與 public.User 的 systemRoles 不同)
     * 例如: VENUE_ADMIN, BOOKING_MANAGER, MEMBER
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_profile_tenant_roles",
        joinColumns = [JoinColumn(name = "user_profile_id")]
    )
    @Column(name = "role")
    val tenantRoles: MutableSet<String> = mutableSetOf(),

    /**
     * 用戶偏好設定 (JSON 格式)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences")
    val preferences: MutableMap<String, Any> = mutableMapOf(),

    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "notes")
    var notes: String? = null
) {

    fun addTenantRole(role: String) {
        tenantRoles.add(role)
    }

    fun removeTenantRole(role: String) {
        tenantRoles.remove(role)
    }

    fun hasTenantRole(role: String): Boolean {
        return tenantRoles.contains(role)
    }

    fun deactivate() {
        isActive = false
    }

    fun activate() {
        isActive = true
    }
}