package com.app.security.repository.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import kotlin.jvm.Transient


enum class Role(val value: String) {
    SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    ADMIN("ROLE_ADMIN"),
    EMPLOYEE("ROLE_EMPLOYEE"),
    USER("ROLE_USER"),
}
data class UserView(
    val id: UUID,
    val userId: UUID,
    val roles: Set<String>,
)


@Entity
@Table(name = "users", schema = "public")
class User(
    @Id
    var id: UUID,

    @Column(nullable = false, unique = true)
    private val username: String,

    @Transient
    @JsonIgnore
    private val password: String?,

    @Column(nullable = false)
    val email: String,

    @Column(nullable = false)
    val provider: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "system_roles", joinColumns = [JoinColumn(name = "user_id")])
    val systemRoles: MutableSet<String> = mutableSetOf(),

    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false,
    ) : UserDetails {

    companion object {
        const val DEFAULT_PLATFORM = "LOCAL"
    }

    fun isLocalAccount(): Boolean {
        return provider == DEFAULT_PLATFORM
    }

    fun verify() {
        this.isVerified = true
    }

    constructor(userId: UUID, name: String, email: String,password: String, provider: String, roles: Iterable<String>) : this(
        id = userId,
        username = name,
        password = password,
        systemRoles = roles.toMutableSet(),
        provider = provider,
        email = email
    )
    @JsonIgnore
    override fun getAuthorities(): Collection<GrantedAuthority> = systemRoles.map { GrantedAuthority { it } }

    override fun getPassword(): String = password ?: ""

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = this.isVerified

    fun assumeRoles(roles: Iterable<String>) {
        this.systemRoles.clear()
        this.systemRoles.addAll(roles)
    }
}