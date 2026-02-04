package com.module.app.core.repository

import com.module.app.security.model.User
import com.module.app.core.models.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, UUID> {

    /**
     * 根據 User 實體查詢租戶層級的用戶檔案（使用跨 schema 查詢）
     */
    @Query(value = """
        SELECT up.*
        FROM user_profiles up
        INNER JOIN public.users u ON up.user_id = u.id
        WHERE u.id = :#{#user.id}
    """, nativeQuery = true)
    fun findByUser(@Param("user") user: User): Optional<UserProfile>

    /**
     * 檢查 User 在當前租戶是否存在（使用跨 schema 查詢）
     */
    @Query(value = """
        SELECT EXISTS(
            SELECT 1
            FROM user_profiles up
            INNER JOIN public.users u ON up.user_id = u.id
            WHERE u.id = :#{#user.id}
        )
    """, nativeQuery = true)
    fun existsByUser(@Param("user") user: User): Boolean

    /**
     * 根據全域用戶ID查詢租戶層級的用戶檔案（向後兼容）
     * 使用原生 SQL 進行跨 schema 查詢
     */
    @Query(value = """
        SELECT up.*
        FROM user_profiles up
        INNER JOIN public.users u ON up.user_id = u.id
        WHERE u.id = :userId
    """, nativeQuery = true)
    fun findByUserId(@Param("userId") userId: UUID): Optional<UserProfile>

}