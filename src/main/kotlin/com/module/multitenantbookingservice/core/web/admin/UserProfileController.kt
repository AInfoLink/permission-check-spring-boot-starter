package com.module.multitenantbookingservice.core.web.admin

import com.module.multitenantbookingservice.core.models.UserProfile
import com.module.multitenantbookingservice.core.service.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/admin/user-profiles")
class UserProfileController(
    private val userProfileService: UserProfileService
) {

    @PostMapping("/{userId}")
    fun createUserProfile(
        @PathVariable userId: UUID,
        @RequestBody profile: UserProfileCreation
    ): ResponseEntity<UserProfile> {
        val userProfile = userProfileService.createUserProfile(userId, profile)
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
        @RequestBody roleOperation: TenantRoleOperation
    ): ResponseEntity<UserProfile> {
        val updatedProfile = userProfileService.addTenantRole(userId, roleOperation)
        return ResponseEntity.ok(updatedProfile)
    }

    @DeleteMapping("/{userId}/roles")
    fun removeTenantRole(
        @PathVariable userId: UUID,
        @RequestBody roleOperation: TenantRoleOperation
    ): ResponseEntity<UserProfile> {
        val updatedProfile = userProfileService.removeTenantRole(userId, roleOperation)
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
        @RequestBody balanceUpdate: WalletBalanceUpdate
    ): ResponseEntity<UserProfile> {
        val updatedProfile = userProfileService.updateWalletBalance(userId, balanceUpdate)
        return ResponseEntity.ok(updatedProfile)
    }
}

