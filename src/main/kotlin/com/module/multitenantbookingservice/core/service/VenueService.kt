package com.module.multitenantbookingservice.core.service

import com.module.multitenantbookingservice.core.models.*
import com.module.multitenantbookingservice.core.repository.VenueGroupRepository
import com.module.multitenantbookingservice.core.repository.VenueRepository
import com.module.multitenantbookingservice.security.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class VenueCreation(
    val name: String,
    val description: String,
    val location: String,
    val venueGroupId: UUID,
    val bookingSlotType: BookingSlotType = BookingSlotType.HALF_HOUR,
    val isScheduleActive: Boolean = true,
    val annotations: MutableMap<String, Any> = mutableMapOf(),
    val scheduleAnnotations: MutableMap<String, Any> = mutableMapOf()
)

data class VenueGroupCreation(
    val name: String,
    val description: String,
    val isDefault: Boolean = false,
    val annotations: MutableMap<String, Any> = mutableMapOf()
)

@Service
@Transactional
class VenueService(
    private val venueRepository: VenueRepository,
    private val venueGroupRepository: VenueGroupRepository
) {

    /**
     * 創建場地組，確保名稱的唯一性
     */
    @Transactional
    fun createVenueGroup(group: VenueGroupCreation): VenueGroup {
        // 檢查是否已經存在相同名稱的場地組
        venueGroupRepository.findByName(group.name).getOrNull()?.let {
            throw VenueGroupAlreadyExists
        }

        val venueGroup = VenueGroup(
            name = group.name,
            description = group.description,
            isDefault = group.isDefault,
            annotations = group.annotations
        )

        return venueGroupRepository.save(venueGroup)
    }

    /**
     * 查詢場地組
     */
    @Transactional(readOnly = true)
    fun getVenueGroup(venueGroupId: UUID): VenueGroup {
        return venueGroupRepository.findById(venueGroupId).getOrNull() ?: throw VenueGroupNotFound
    }

    /**
     * 根據名稱查詢場地組
     */
    @Transactional(readOnly = true)
    fun getVenueGroupByName(name: String): VenueGroup {
        return venueGroupRepository.findByName(name).getOrNull() ?: throw VenueGroupNotFound
    }

    /**
     * 查詢預設場地組
     */
    @Transactional(readOnly = true)
    fun getDefaultVenueGroup(): VenueGroup {
        return venueGroupRepository.findByIsDefaultTrue().getOrNull() ?: throw VenueGroupNotFound
    }

    /**
     * 查詢所有場地組
     */
    @Transactional(readOnly = true)
    fun getAllVenueGroups(): List<VenueGroup> {
        return venueGroupRepository.findAll()
    }

    /**
     * 刪除場地組（會一併刪除關聯的場地）
     */
    @Transactional
    fun deleteVenueGroup(venueGroupId: UUID) {
        val venueGroup = venueGroupRepository.findById(venueGroupId).getOrNull() ?: return
        venueGroupRepository.delete(venueGroup)
    }

    /**
     * 創建場地，使用數據庫層級參照完整性
     */
    fun createVenue(venue: VenueCreation): Venue {
        // 獲取場地組實體，如果不存在會拋出異常
        val venueGroup = venueGroupRepository.findById(venue.venueGroupId).getOrNull()
            ?: throw VenueGroupNotFound

        // 檢查是否已經存在相同名稱的場地
        venueRepository.findByName(venue.name).getOrNull()?.let {
            throw VenueAlreadyExists
        }

        val scheduleConfig = VenueScheduleConfig(
            bookingSlotType = venue.bookingSlotType,
            isActive = venue.isScheduleActive,
            annotations = venue.scheduleAnnotations
        )

        val newVenue = Venue(
            name = venue.name,
            description = venue.description,
            location = venue.location,
            venueGroup = venueGroup,
            scheduleConfig = scheduleConfig,
            annotations = venue.annotations
        )

        return venueRepository.save(newVenue)
    }

    /**
     * 查詢場地
     */
    @Transactional(readOnly = true)
    fun getVenue(venueId: UUID): Venue {
        return venueRepository.findById(venueId).getOrNull() ?: throw VenueNotFound
    }

    /**
     * 根據名稱查詢場地
     */
    @Transactional(readOnly = true)
    fun getVenueByName(name: String): Venue {
        return venueRepository.findByName(name).getOrNull() ?: throw VenueNotFound
    }

    /**
     * 根據場地組查詢所有場地
     */
    @Transactional(readOnly = true)
    fun getVenuesByGroup(venueGroupId: UUID): List<Venue> {
        return venueRepository.findByVenueGroupId(venueGroupId)
    }

    /**
     * 查詢所有場地
     */
    @Transactional(readOnly = true)
    fun getAllVenues(): List<Venue> {
        return venueRepository.findAll()
    }

    /**
     * 更新場地信息
     */
    @Transactional
    fun updateVenue(venueId: UUID, name: String?, description: String?, location: String?): Venue {
        val venue = venueRepository.findById(venueId).getOrNull() ?: throw VenueNotFound

        name?.let { venue.name = it }
        description?.let { venue.description = it }
        location?.let { venue.location = it }

        return venueRepository.save(venue)
    }

    /**
     * 更新場地排程配置
     */
    @Transactional
    fun updateVenueScheduleConfig(venueId: UUID, bookingSlotType: BookingSlotType?, isActive: Boolean?): Venue {
        val venue = venueRepository.findById(venueId).getOrNull() ?: throw VenueNotFound

        bookingSlotType?.let { venue.scheduleConfig.bookingSlotType = it }
        isActive?.let { venue.scheduleConfig.isActive = it }

        return venueRepository.save(venue)
    }

    /**
     * 移動場地到其他場地組
     */
    @Transactional
    fun moveVenueToGroup(venueId: UUID, newVenueGroupId: UUID): Venue {
        val venue = venueRepository.findById(venueId).getOrNull() ?: throw VenueNotFound
        val newVenueGroup = venueGroupRepository.findById(newVenueGroupId).getOrNull()
            ?: throw VenueGroupNotFound.withDetails("Target VenueGroup with id $newVenueGroupId not found")

        venue.venueGroup = newVenueGroup
        return venueRepository.save(venue)
    }

    /**
     * 刪除場地
     */
    @Transactional
    fun deleteVenue(venueId: UUID) {
        val venue = venueRepository.findById(venueId).getOrNull() ?: return
        venueRepository.delete(venue)
    }
}