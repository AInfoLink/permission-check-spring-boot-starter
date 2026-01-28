package com.module.multitenantbookingservice.core.models

import com.module.multitenantbookingservice.utils.StringSetConverter
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenant_roles")
class TenantRole(
    @Id
    @Column(name = "role_id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "role_name", nullable = false, length = 100)
    val name: String,

    @ManyToMany(mappedBy = "tenantRoles")
    val userProfiles: MutableSet<UserProfile> = mutableSetOf(),

    @Column(name = "role_description", length = 500)
    val description: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Convert(converter = StringSetConverter::class)
    @Column(name = "permissions", columnDefinition = "TEXT")
    val permissions: MutableSet<String> = mutableSetOf()
)