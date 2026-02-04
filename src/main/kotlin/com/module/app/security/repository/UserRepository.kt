package com.module.app.security.repository

import com.module.app.security.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun findByUsername(username: String): Optional<User>
    fun findByUsernameContaining(keyword: String, pageable: Pageable): Page<User>

    /**
     * 檢查用戶是否存在 (用於 UserProfile 的參照完整性檢查)
     */
    override fun existsById(id: UUID): Boolean
}