package com.module.multitenantbookingservice.core.service

import com.module.multitenantbookingservice.core.models.BookingSlotType
import com.module.multitenantbookingservice.core.models.Venue
import com.module.multitenantbookingservice.core.models.VenueGroup
import com.module.multitenantbookingservice.core.models.VenueScheduleConfig
import com.module.multitenantbookingservice.core.repository.VenueGroupRepository
import com.module.multitenantbookingservice.core.repository.VenueRepository
import com.module.multitenantbookingservice.security.VenueAlreadyExists
import com.module.multitenantbookingservice.security.VenueGroupAlreadyExists
import com.module.multitenantbookingservice.security.VenueGroupNotFound
import com.module.multitenantbookingservice.security.VenueNotFound
import com.module.multitenantbookingservice.security.annotation.Permission
import com.module.multitenantbookingservice.security.annotation.Require
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
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

data class VenueUpdate(
    val name: String? = null,
    val description: String? = null,
    val location: String? = null,
    val venueGroupId: UUID? = null,
    val bookingSlotType: BookingSlotType? = null,
    val isScheduleActive: Boolean? = null,
    val scheduleAnnotations: MutableMap<String, Any>? = null
)

data class VenueGroupCreation(
    val name: String,
    val description: String,
    val isDefault: Boolean = false,
    val annotations: MutableMap<String, Any> = mutableMapOf()
)

data class VenueGroupUpdate(
    val name: String? = null,
    val description: String? = null,
    val isDefault: Boolean? = null,
    val annotations: MutableMap<String, Any> = mutableMapOf()
)

data class VenueQuery(
    val name: String? = null,
    val description: String? = null,
    val location: String? = null,
    val venueGroupId: UUID? = null,
    val venueGroupName: String? = null,
    val bookingSlotType: BookingSlotType? = null,
    val isScheduleActive: Boolean? = null
)

interface VenueService {
    // VenueGroup operations
    fun createVenueGroup(group: VenueGroupCreation): VenueGroup
    fun getVenueGroup(venueGroupId: UUID): VenueGroup
    fun getVenueGroupByName(name: String): VenueGroup
    fun getDefaultVenueGroup(): VenueGroup
    fun getAllVenueGroups(): List<VenueGroup>
    fun updateVenueGroup(venueGroupId: UUID, update: VenueGroupUpdate): VenueGroup
    fun deleteVenueGroup(venueGroupId: UUID)

    // Venue operations
    fun createVenue(venue: VenueCreation): Venue
    fun getVenue(venueId: UUID): Venue
    fun getVenueByName(name: String): Venue
    fun getVenuesByGroup(venueGroupId: UUID): List<Venue>
    fun getAllVenues(): List<Venue>
    fun searchVenues(query: VenueQuery, pageable: Pageable): Page<Venue>
    fun updateVenue(venueId: UUID, update: VenueUpdate): Venue
    fun deleteVenue(venueId: UUID)
}

@Service
@Transactional
class DefaultVenueService(
    private val venueRepository: VenueRepository,
    private val venueGroupRepository: VenueGroupRepository
): VenueService {

    private val logger = LoggerFactory.getLogger(DefaultVenueService::class.java)

    /**
     * Create venue group, ensuring name uniqueness
     */
    @Require(Permission.VENUE_GROUPS_CREATE)
    @Transactional
    override fun createVenueGroup(group: VenueGroupCreation): VenueGroup {
        logger.info("Creating venue group with name: ${group.name}, isDefault: ${group.isDefault}")

        // Check if venue group with same name already exists
        venueGroupRepository.findByName(group.name).getOrNull()?.let {
            logger.warn("Venue group creation failed - name already exists: ${group.name}")
            throw VenueGroupAlreadyExists
        }

        val venueGroup = VenueGroup(
            name = group.name,
            description = group.description,
            isDefault = group.isDefault,
            annotations = group.annotations
        )

        val savedVenueGroup = venueGroupRepository.save(venueGroup)
        logger.info("Venue group created successfully - ID: ${savedVenueGroup.id}, name: ${savedVenueGroup.name}")
        return savedVenueGroup
    }

    /**
     * Query venue group by ID
     */
    @Transactional(readOnly = true)
    override fun getVenueGroup(venueGroupId: UUID): VenueGroup {
        return venueGroupRepository.findById(venueGroupId).getOrNull() ?: throw VenueGroupNotFound
    }

    /**
     * Query venue group by name
     */
    @Transactional(readOnly = true)
    override fun getVenueGroupByName(name: String): VenueGroup {
        return venueGroupRepository.findByName(name).getOrNull() ?: throw VenueGroupNotFound
    }

    /**
     * Query default venue group
     */
    @Transactional(readOnly = true)
    override fun getDefaultVenueGroup(): VenueGroup {
        return venueGroupRepository.findByIsDefaultTrue().getOrNull() ?: throw VenueGroupNotFound
    }

    /**
     * Query all venue groups
     */
    @Transactional(readOnly = true)
    override fun getAllVenueGroups(): List<VenueGroup> {
        return venueGroupRepository.findAll()
    }

    /**
     * Update venue group information
     */
    @Transactional
    override fun updateVenueGroup(venueGroupId: UUID, update: VenueGroupUpdate): VenueGroup {
        val venueGroup = venueGroupRepository.findById(venueGroupId).getOrNull() ?: throw VenueGroupNotFound

        update.name?.let { venueGroup.name = it }
        update.description?.let { venueGroup.description = it }
        update.isDefault?.let { venueGroup.isDefault = it }
        update.annotations.let { venueGroup.annotations.putAll(it) }

        return venueGroupRepository.save(venueGroup)
    }

    /**
     * Delete venue group (will also delete associated venues)
     */
    @Transactional
    override fun deleteVenueGroup(venueGroupId: UUID) {
        val venueGroup = venueGroupRepository.findById(venueGroupId).getOrNull() ?: return
        venueGroupRepository.delete(venueGroup)
    }

    /**
     * Create venue with database-level referential integrity
     */
    @Require(Permission.VENUES_CREATE)
    @Transactional
    override fun createVenue(venue: VenueCreation): Venue {
        logger.info("Creating venue: ${venue.name} in group: ${venue.venueGroupId}")

        // Get venue group entity, throw exception if not found
        val venueGroup = venueGroupRepository.findById(venue.venueGroupId).getOrNull() ?: run {
            logger.error("Venue creation failed - venue group not found: ${venue.venueGroupId}")
            throw VenueGroupNotFound
        }

        // Check if venue with same name already exists
        venueRepository.findByName(venue.name).getOrNull()?.let {
            logger.warn("Venue creation failed - name already exists: ${venue.name}")
            throw VenueAlreadyExists
        }

        val scheduleConfig = VenueScheduleConfig(
            bookingSlotType = venue.bookingSlotType,
            isActive = venue.isScheduleActive,
        )

        val newVenue = Venue(
            name = venue.name,
            description = venue.description,
            location = venue.location,
            venueGroup = venueGroup,
            scheduleConfig = scheduleConfig,
            annotations = venue.annotations
        )

        val savedVenue = venueRepository.save(newVenue)
        logger.info("Venue created successfully - ID: ${savedVenue.id}, name: ${savedVenue.name}")
        return savedVenue
    }

    /**
     * Query venue
     */
    @Require(Permission.VENUES_READ)
    @Transactional(readOnly = true)
    override fun getVenue(venueId: UUID): Venue {
        return venueRepository.findById(venueId).getOrNull() ?: throw VenueNotFound
    }

    /**
     * Query venue by name
     */
    @Require(Permission.VENUES_READ)
    @Transactional(readOnly = true)
    override fun getVenueByName(name: String): Venue {
        return venueRepository.findByName(name).getOrNull() ?: throw VenueNotFound
    }

    /**
     * Query all venues by venue group
     */
    @Require(Permission.VENUES_READ)
    @Transactional(readOnly = true)
    override fun getVenuesByGroup(venueGroupId: UUID): List<Venue> {
        return venueRepository.findByVenueGroupId(venueGroupId)
    }

    /**
     * Query all venues
     */
    @Require(Permission.VENUES_READ)
    @Transactional(readOnly = true)
    override fun getAllVenues(): List<Venue> {
        return venueRepository.findAll()
    }

    /**
     * Search venues (supports pagination and multiple query criteria)
     */
    @Require(Permission.VENUES_READ)
    @Transactional(readOnly = true)
    override fun searchVenues(query: VenueQuery, pageable: Pageable): Page<Venue> {
        return venueRepository.searchVenues(
            name = query.name,
            description = query.description,
            location = query.location,
            venueGroupId = query.venueGroupId,
            venueGroupName = query.venueGroupName,
            bookingSlotType = query.bookingSlotType,
            isScheduleActive = query.isScheduleActive,
            pageable = pageable
        )
    }

    /**
     * Update venue information
     */
    @Require(Permission.VENUES_UPDATE)
    @Transactional
    override fun updateVenue(venueId: UUID, update: VenueUpdate): Venue {
        val venue = venueRepository.findById(venueId).getOrNull() ?: throw VenueNotFound

        update.name?.let { venue.name = it }
        update.description?.let { venue.description = it }
        update.location?.let { venue.location = it }

        update.venueGroupId?.let { newVenueGroupId ->
            val newVenueGroup = venueGroupRepository.findById(newVenueGroupId).getOrNull()
                ?: throw VenueGroupNotFound
            venue.venueGroup = newVenueGroup
        }

        update.bookingSlotType?.let { venue.scheduleConfig.bookingSlotType = it }
        update.isScheduleActive?.let { venue.scheduleConfig.isActive = it }

        return venueRepository.save(venue)
    }

    /**
     * Update venue schedule configuration
     */
    @Require(Permission.VENUES_UPDATE)
    @Transactional
    fun updateVenueScheduleConfig(venueId: UUID, bookingSlotType: BookingSlotType?, isActive: Boolean?): Venue {
        val venue = venueRepository.findById(venueId).getOrNull() ?: throw VenueNotFound

        bookingSlotType?.let { venue.scheduleConfig.bookingSlotType = it }
        isActive?.let { venue.scheduleConfig.isActive = it }

        return venueRepository.save(venue)
    }

    /**
     * Move venue to different venue group
     */
    @Require(Permission.VENUES_UPDATE)
    @Transactional
    fun moveVenueToGroup(venueId: UUID, newVenueGroupId: UUID): Venue {
        val venue = venueRepository.findById(venueId).getOrNull() ?: throw VenueNotFound
        val newVenueGroup = venueGroupRepository.findById(newVenueGroupId).getOrNull()
            ?: throw VenueGroupNotFound.withDetails("Target VenueGroup with id $newVenueGroupId not found")

        venue.venueGroup = newVenueGroup
        return venueRepository.save(venue)
    }

    /**
     * Delete venue
     */
    @Require(Permission.VENUES_DELETE)
    @Transactional
    override fun deleteVenue(venueId: UUID) {
        val venue = venueRepository.findById(venueId).getOrNull() ?: return
        venueRepository.delete(venue)
    }
}