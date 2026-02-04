package com.module.app.core.repository

import com.module.app.core.models.BookingSlotType
import com.module.app.core.models.Venue
import com.module.app.core.models.VenueGroup
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface VenueRepository: ListCrudRepository<Venue, UUID> {
    fun findByName(name: String): Optional<Venue>
    fun findByVenueGroup(venueGroup: VenueGroup): List<Venue>
    fun findByVenueGroupId(venueGroupId: UUID): List<Venue>

    @Query("""
        SELECT v FROM Venue v
        WHERE (:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:description IS NULL OR LOWER(v.description) LIKE LOWER(CONCAT('%', :description, '%')))
        AND (:location IS NULL OR LOWER(v.location) LIKE LOWER(CONCAT('%', :location, '%')))
        AND (:venueGroupId IS NULL OR v.venueGroup.id = :venueGroupId)
        AND (:venueGroupName IS NULL OR LOWER(v.venueGroup.name) LIKE LOWER(CONCAT('%', :venueGroupName, '%')))
        AND (:bookingSlotType IS NULL OR v.scheduleConfig.bookingSlotType = :bookingSlotType)
        AND (:isScheduleActive IS NULL OR v.scheduleConfig.isActive = :isScheduleActive)
    """)
    fun searchVenues(
        @Param("name") name: String?,
        @Param("description") description: String?,
        @Param("location") location: String?,
        @Param("venueGroupId") venueGroupId: UUID?,
        @Param("venueGroupName") venueGroupName: String?,
        @Param("bookingSlotType") bookingSlotType: BookingSlotType?,
        @Param("isScheduleActive") isScheduleActive: Boolean?,
        pageable: Pageable
    ): Page<Venue>
}