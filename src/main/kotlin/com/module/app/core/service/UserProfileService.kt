package com.module.app.core.service

import com.module.app.core.models.UserProfile
import com.module.app.core.repository.TenantRoleRepository
import com.module.app.core.repository.UserProfileRepository
import com.module.app.security.TenantRoleNotFound
import com.module.app.security.UserNotFound
import com.module.app.security.UserProfileAlreadyExists
import com.module.app.security.UserProfileNotCreated
import io.github.common.permission.annotation.Require
import com.module.app.security.repository.UserRepository
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class UserProfileCreation(
    val tenantRoleIds: MutableSet<UUID> = mutableSetOf(),
    val walletBalance: Int = 0,
    val isActive: Boolean = true
)

data class UserProfileUpdate(
    val tenantRoleIds: MutableSet<UUID>? = null,
    val walletBalance: Int? = null,
    val isActive: Boolean? = null
)

data class TenantRoleOperation(
    val roleId: UUID
)

data class WalletBalanceUpdate(
    val walletBalance: Int
)

data class PermissionReloadEvent(
    val userId: UUID
): ApplicationEvent(userId)

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

    // Internal method for permission checking (no @Require annotation)
    fun getUserProfileInternal(userId: UUID): UserProfile
}

@Service
@Transactional
class DefaultUserProfileService(
    private val userProfileRepository: UserProfileRepository,
    private val userRepository: UserRepository,
    private val tenantRoleRepository: TenantRoleRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
): UserProfileService {

    /**
     * 創建用戶檔案，使用數據庫層級參照完整性
     */
    @Require("users:create")
    @Transactional
    override fun createUserProfile(userId: UUID, profile: UserProfileCreation): UserProfile {
        // 獲取 User 實體，如果不存在會拋出異常
        val user = userRepository.findById(userId).getOrNull() ?: throw UserNotFound

        // 檢查是否已經存在該用戶的檔案
        userProfileRepository.findByUser(user).getOrNull()?.let {
            throw UserProfileAlreadyExists
        }

        // 根據tenantRoleIds獲取TenantRole對象
        val tenantRoles = if (profile.tenantRoleIds.isNotEmpty()) {
            tenantRoleRepository.findAllById(profile.tenantRoleIds).toMutableSet()
        } else {
            mutableSetOf()
        }

        val userProfile = UserProfile(
            user = user,
            tenantRoles = tenantRoles,
            walletBalance = profile.walletBalance,
            isActive = profile.isActive
        )

        return userProfileRepository.save(userProfile)
    }

    /**
     * 查詢用戶檔案，由於使用外鍵約束，不再需要應用層驗證
     */
    @Require("users:read")
    @Transactional(readOnly = true)
    override fun getUserProfile(userId: UUID): UserProfile {
        return getUserProfileInternal(userId)
    }

    /**
     * 內部方法：查詢用戶檔案（不進行權限檢查，用於權限驗證流程）
     */
    @Transactional(readOnly = true)
    override fun getUserProfileInternal(userId: UUID): UserProfile {
        return userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
    }

    /**
     * 更新用戶檔案信息
     */
    @Require("users:update")
    @Transactional
    override fun updateUserProfile(userId: UUID, update: UserProfileUpdate): UserProfile {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated

        update.tenantRoleIds?.let { newRoleIds ->
            userProfile.tenantRoles.clear()
            if (newRoleIds.isNotEmpty()) {
                val newRoles = tenantRoleRepository.findAllById(newRoleIds)
                userProfile.tenantRoles.addAll(newRoles)
            }
        }
        update.walletBalance?.let { userProfile.walletBalance = it }
        update.isActive?.let { userProfile.isActive = it }

        return userProfileRepository.save(userProfile)
    }

    /**
     * 刪除用戶檔案（保留全域用戶）
     */
    @Require("users:delete")
    @Transactional
    override fun deleteUserProfile(userId: UUID) {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: return
        userProfileRepository.delete(userProfile)
    }

    /**
     * 添加租戶角色
     */
    @Require("roles:assign")
    @Transactional
    override fun addTenantRole(userId: UUID, roleOperation: TenantRoleOperation): UserProfile {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
        val tenantRole = tenantRoleRepository.findById(roleOperation.roleId).getOrNull() ?: throw TenantRoleNotFound
        userProfile.addTenantRole(tenantRole)
        applicationEventPublisher.publishEvent(PermissionReloadEvent(userId))
        return userProfileRepository.save(userProfile)
    }

    /**
     * 移除租戶角色
     */
    @Require("roles:unassign")
    @Transactional
    override fun removeTenantRole(userId: UUID, roleOperation: TenantRoleOperation): UserProfile {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
        val tenantRole = tenantRoleRepository.findById(roleOperation.roleId).getOrNull() ?: throw TenantRoleNotFound
        userProfile.removeTenantRole(tenantRole)
        applicationEventPublisher.publishEvent(PermissionReloadEvent(userId))
        return userProfileRepository.save(userProfile)
    }

    /**
     * 更新錢包餘額
     */
    @Require("wallets:adjust")
    @Transactional
    override fun updateWalletBalance(userId: UUID, balanceUpdate: WalletBalanceUpdate): UserProfile {
        val userProfile = userProfileRepository.findByUserId(userId).getOrNull() ?: throw UserProfileNotCreated
        userProfile.walletBalance = balanceUpdate.walletBalance
        return userProfileRepository.save(userProfile)
    }
}