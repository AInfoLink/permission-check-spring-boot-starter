package com.module.multitenantbookingservice.core.repository

import com.app.security.repository.model.User
import com.module.multitenantbookingservice.core.models.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
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

}