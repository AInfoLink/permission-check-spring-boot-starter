package com.module.multitenantbookingservice.core.models

import com.module.multitenantbookingservice.security.model.User
import com.module.multitenantbookingservice.security.permission.HasResourceOwner
import jakarta.persistence.*
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
     * Uses cross schema foreign key to ensure referential integrity
     */
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_user_profiles_user_id")
    )
    val user: User,

    /**
     * Tenant-specific roles (different from systemRoles in public.User)
     * Examples: VENUE_ADMIN, BOOKING_MANAGER, MEMBER
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_profile_tenant_roles",
        joinColumns = [JoinColumn(name = "user_profile_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var tenantRoles: MutableSet<TenantRole> = mutableSetOf(),

    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "membership_id", nullable = true)
    val membership: Membership? = null,

    @Column(name = "wallet_balance", nullable = false)
    var walletBalance: Int = 0,

    ): HasResourceOwner {

    fun addTenantRole(role: TenantRole) {
        tenantRoles.add(role)
    }

    fun removeTenantRole(role: TenantRole) {
        tenantRoles.remove(role)
    }

    fun hasTenantRole(roleName: String): Boolean {
        return tenantRoles.any { it.name == roleName }
    }

    fun hasTenantRole(role: TenantRole): Boolean {
        return tenantRoles.contains(role)
    }

    fun deactivate() {
        isActive = false
    }

    fun activate() {
        isActive = true
    }

    override fun getResourceOwnerId(): String {
        return user.id.toString()
    }


}