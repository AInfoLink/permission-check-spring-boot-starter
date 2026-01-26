package com.module.multitenantbookingservice.core.service

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

data class UserProfileCreation(
    val tenantRoles: MutableSet<String> = mutableSetOf(),
    val walletBalance: Int = 0,
    val isActive: Boolean = true
)

data class UserProfileUpdate(
    val tenantRoles: MutableSet<String>? = null,
    val walletBalance: Int? = null,
    val isActive: Boolean? = null
)

data class TenantRoleOperation(
    val role: String
)

data class WalletBalanceUpdate(
    val walletBalance: Int
)

interface UserProfileService {
    fun createUserProfile(userId: UUID, profile: UserProfileCreation): UserProfile
    fun getUserProfile(userId: UUID): UserProfile
    fun updateUserProfile(userId: UUID, update: UserProfileUpdate): UserProfile
    fun deleteUserProfile(userId: UUID)

    // Tenant role operations
    fun addTenantRole(userId: UUID, roleOperation: TenantRoleOperation): UserProfile
    fun removeTenantRole(userId: UUID, roleOperation: TenantRoleOperation): UserProfile

    // Wallet balance operation
    fun updateWalletBalance(userId: UUID, balanceUpdate: WalletBalanceUpdate): UserProfile
}

@Service
@Transactional
class DefaultUserProfileService(
    private val userProfileRepository: UserProfileRepository,
    private val userRepository: UserRepository
): UserProfileService {

    /**
     * 創建用戶檔案，使用數據庫層級參照完整性
     */
    @Transactional
    override fun createUserProfile(userId: UUID, profile: UserProfileCreation): UserProfile {
        // 獲取 User 實體，如果不存在會拋出異常
        val user = userRepository.findById(userId).getOrNull() ?: throw UserNotFound

        // 檢查是否已經存在該用戶的檔案
        userProfileRepository.findByUser(user).getOrNull()?.let {
            throw UserProfileAlreadyExists
        }

        val userProfile = UserProfile(
            user = user,
            tenantRoles = profile.tenantRoles,
            walletBalance = profile.walletBalance,
            isActive = profile.isActive
        )

        return userProfileRepository.save(userProfile)
    }

    /**
     * 查詢用戶檔案，由於使用外鍵約束，不再需要應用層驗證
     */
    @Transactional(readOnly = true)
    override fun getUserProfile(userId: UUID): UserProfile {
        return userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
    }

    /**
     * 更新用戶檔案信息
     */
    @Transactional
    override fun updateUserProfile(userId: UUID, update: UserProfileUpdate): UserProfile {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated

        update.tenantRoles?.let { newRoles ->
            userProfile.tenantRoles.clear()
            userProfile.tenantRoles.addAll(newRoles)
        }
        update.walletBalance?.let { userProfile.walletBalance = it }
        update.isActive?.let { userProfile.isActive = it }

        return userProfileRepository.save(userProfile)
    }

    /**
     * 刪除用戶檔案（保留全域用戶）
     */
    @Transactional
    override fun deleteUserProfile(userId: UUID) {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: return
        userProfileRepository.delete(userProfile)
    }

    /**
     * 添加租戶角色
     */
    @Transactional
    override fun addTenantRole(userId: UUID, roleOperation: TenantRoleOperation): UserProfile {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
        userProfile.addTenantRole(roleOperation.role)
        return userProfileRepository.save(userProfile)
    }

    /**
     * 移除租戶角色
     */
    @Transactional
    override fun removeTenantRole(userId: UUID, roleOperation: TenantRoleOperation): UserProfile {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
        userProfile.removeTenantRole(roleOperation.role)
        return userProfileRepository.save(userProfile)
    }

    /**
     * 更新錢包餘額
     */
    @Transactional
    override fun updateWalletBalance(userId: UUID, balanceUpdate: WalletBalanceUpdate): UserProfile {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
        userProfile.walletBalance = balanceUpdate.walletBalance
        return userProfileRepository.save(userProfile)
    }
}