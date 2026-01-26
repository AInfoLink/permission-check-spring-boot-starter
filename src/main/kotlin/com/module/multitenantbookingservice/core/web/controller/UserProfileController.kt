package com.module.multitenantbookingservice.core.web.controller

import com.module.multitenantbookingservice.core.models.UserProfile
import com.module.multitenantbookingservice.core.service.UserProfileCreation
import com.module.multitenantbookingservice.core.service.UserProfileService
import com.module.multitenantbookingservice.core.service.UserProfileUpdate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/user-profiles")
class UserProfileController(
    private val userProfileService: UserProfileService
) {

    @PostMapping
    fun createUserProfile(@RequestBody request: CreateUserProfileRequest): ResponseEntity<UserProfile> {
        val creation = UserProfileCreation(
            tenantRoles = request.tenantRoles,
            walletBalance = request.walletBalance,
            isActive = request.isActive
        )
        val userProfile = userProfileService.createUserProfile(request.userId, creation)
        return ResponseEntity(userProfile, HttpStatus.CREATED)
    }

    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: UUID): ResponseEntity<UserProfile> {
        val userProfile = userProfileService.getUserProfile(userId)
        return ResponseEntity.ok(userProfile)
    }

    @PutMapping("/{userId}")
    fun updateUserProfile(
        @PathVariable userId: UUID,
        @RequestBody update: UserProfileUpdate
    ): ResponseEntity<UserProfile> {
        val userProfile = userProfileService.updateUserProfile(userId, update)
        return ResponseEntity.ok(userProfile)
    }

    @DeleteMapping("/{userId}")
    fun deleteUserProfile(@PathVariable userId: UUID): ResponseEntity<Map<String, String>> {
        userProfileService.deleteUserProfile(userId)
        return ResponseEntity.ok(mapOf("message" to "User profile deleted successfully"))
    }

    @PostMapping("/{userId}/roles")
    fun addTenantRole(
        @PathVariable userId: UUID,
        @RequestBody request: TenantRoleRequest
    ): ResponseEntity<UserProfile> {
        val userProfile = userProfileService.getUserProfile(userId)
        userProfile.addTenantRole(request.role)
        val updatedProfile = userProfileService.updateUserProfile(
            userId,
            UserProfileUpdate(tenantRoles = userProfile.tenantRoles)
        )
        return ResponseEntity.ok(updatedProfile)
    }

    @DeleteMapping("/{userId}/roles")
    fun removeTenantRole(
        @PathVariable userId: UUID,
        @RequestBody request: TenantRoleRequest
    ): ResponseEntity<UserProfile> {
        val userProfile = userProfileService.getUserProfile(userId)
        userProfile.removeTenantRole(request.role)
        val updatedProfile = userProfileService.updateUserProfile(
            userId,
            UserProfileUpdate(tenantRoles = userProfile.tenantRoles)
        )
        return ResponseEntity.ok(updatedProfile)
    }

    @PostMapping("/{userId}/deactivate")
    fun deactivateUserProfile(@PathVariable userId: UUID): ResponseEntity<UserProfile> {
        val updatedProfile = userProfileService.updateUserProfile(
            userId,
            UserProfileUpdate(isActive = false)
        )
        return ResponseEntity.ok(updatedProfile)
    }

    @PostMapping("/{userId}/activate")
    fun activateUserProfile(@PathVariable userId: UUID): ResponseEntity<UserProfile> {
        val updatedProfile = userProfileService.updateUserProfile(
            userId,
            UserProfileUpdate(isActive = true)
        )
        return ResponseEntity.ok(updatedProfile)
    }

    @PatchMapping("/{userId}/wallet-balance")
    fun updateWalletBalance(
        @PathVariable userId: UUID,
        @RequestBody request: WalletBalanceUpdateRequest
    ): ResponseEntity<UserProfile> {
        val updatedProfile = userProfileService.updateUserProfile(
            userId,
            UserProfileUpdate(walletBalance = request.walletBalance)
        )
        return ResponseEntity.ok(updatedProfile)
    }
}

data class CreateUserProfileRequest(
    val userId: UUID,
    val tenantRoles: MutableSet<String> = mutableSetOf(),
    val walletBalance: Int = 0,
    val isActive: Boolean = true
)

data class TenantRoleRequest(
    val role: String
)

data class WalletBalanceUpdateRequest(
    val walletBalance: Int
)