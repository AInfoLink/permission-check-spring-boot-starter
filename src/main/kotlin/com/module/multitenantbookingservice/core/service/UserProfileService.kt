package com.module.multitenantbookingservice.core.service

import com.app.security.repository.model.User
import com.module.multitenantbookingservice.core.models.UserProfile
import com.module.multitenantbookingservice.core.repository.UserProfileRepository
import com.module.multitenantbookingservice.security.UserNotFound
import com.module.multitenantbookingservice.security.UserProfileAlreadyExists
import com.module.multitenantbookingservice.security.UserProfileNotCreated
import com.module.multitenantbookingservice.security.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class UserProfileService(
    private val userProfileRepository: UserProfileRepository,
    private val userRepository: UserRepository
) {

    /**
     * 創建用戶檔案，使用數據庫層級參照完整性
     */
    fun createUserProfile(
        userId: UUID,
        tenantRoles: Set<String> = emptySet()
    ): UserProfile {
        // 獲取 User 實體，如果不存在會拋出異常
        val user = userRepository.findById(userId).getOrNull() ?: throw UserNotFound

        // 檢查是否已經存在該租戶的用戶檔案
        userProfileRepository.findByUser(user).getOrNull() ?: throw UserProfileAlreadyExists


        val userProfile = UserProfile(
            user = user,
            tenantRoles = tenantRoles.toMutableSet()
        )

        return userProfileRepository.save(userProfile)
    }

    /**
     * 查詢用戶檔案，由於使用外鍵約束，不再需要應用層驗證
     */
    @Transactional(readOnly = true)
    fun getUserProfile(userId: UUID): UserProfile {
        return userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
    }

    /**
     * 刪除用戶檔案（保留全域用戶）
     */
    @Transactional
    fun deleteUserProfile(userId: UUID) {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: return
        userProfileRepository.delete(userProfile)
    }
}