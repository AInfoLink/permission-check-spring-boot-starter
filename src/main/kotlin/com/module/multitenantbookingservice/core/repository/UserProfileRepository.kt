package com.module.multitenantbookingservice.core.repository

import com.app.security.repository.model.User
import com.module.multitenantbookingservice.core.models.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, UUID> {

    /**
     * 根據 User 實體查詢租戶層級的用戶檔案（使用 foreign key）
     */
    fun findByUser(user: User): Optional<UserProfile>

    /**
     * 檢查 User 在當前租戶是否存在
     */
    fun existsByUser(user: User): Boolean

    /**
     * 根據全域用戶ID查詢租戶層級的用戶檔案（向後兼容）
     */
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    fun findByUserId(@Param("userId") userId: UUID): Optional<UserProfile>

    /**
     * 檢查用戶在當前租戶是否存在（向後兼容）
     */
    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END FROM UserProfile up WHERE up.user.id = :userId")
    fun existsByUserId(@Param("userId") userId: UUID): Boolean

    /**
     * 查詢擁有特定租戶角色的用戶
     */
    @Query("SELECT up FROM UserProfile up JOIN up.tenantRoles r WHERE r = :role")
    fun findByTenantRole(@Param("role") role: String): List<UserProfile>

    /**
     * 查詢特定時間後加入的用戶
     */
    @Query("SELECT up FROM UserProfile up WHERE up.joinedAt >= :since")
    fun findUsersJoinedSince(@Param("since") since: LocalDateTime): List<UserProfile>
}