package com.module.multitenantbookingservice.core.web.controller

import com.module.multitenantbookingservice.core.models.Venue
import com.module.multitenantbookingservice.core.models.BookingSlotType
import com.module.multitenantbookingservice.core.service.VenueCreation
import com.module.multitenantbookingservice.core.service.VenueQuery
import com.module.multitenantbookingservice.core.service.VenueService
import com.module.multitenantbookingservice.core.service.VenueUpdate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/venues")
class VenueController(
    private val venueService: VenueService
) {

    @PostMapping
    fun createVenue(@RequestBody request: VenueCreation): ResponseEntity<Venue> {
        val venue = venueService.createVenue(request)
        return ResponseEntity(venue, HttpStatus.CREATED)
    }

    @GetMapping("/{venueId}")
    fun getVenue(@PathVariable venueId: UUID): ResponseEntity<Venue> {
        val venue = venueService.getVenue(venueId)
        return ResponseEntity.ok(venue)
    }

    @GetMapping
    fun getAllVenues(): ResponseEntity<List<Venue>> {
        val venues = venueService.getAllVenues()
        return ResponseEntity.ok(venues)
    }

    @GetMapping("/search")
    fun searchVenues(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) venueGroupId: UUID?,
        @RequestParam(required = false) venueGroupName: String?,
        @RequestParam(required = false) bookingSlotType: BookingSlotType?,
        @RequestParam(required = false) isScheduleActive: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "name") sortBy: String,
        @RequestParam(defaultValue = "asc") sort: Sort.Direction
    ): ResponseEntity<Page<Venue>> {
        val query = VenueQuery(
            name = name,
            description = description,
            location = location,
            venueGroupId = venueGroupId,
            venueGroupName = venueGroupName,
            bookingSlotType = bookingSlotType,
            isScheduleActive = isScheduleActive
        )

        val pageable = PageRequest.of(page, size, Sort.by(sort, sortBy))

        val venues = venueService.searchVenues(query, pageable)
        return ResponseEntity.ok(venues)
    }

    @PutMapping("/{venueId}")
    fun updateVenue(
        @PathVariable venueId: UUID,
        @RequestBody update: VenueUpdate
    ): ResponseEntity<Venue> {
        val venue = venueService.updateVenue(venueId, update)
        return ResponseEntity.ok(venue)
    }

    @PatchMapping("/{venueId}/schedule-config")
    fun updateVenueScheduleConfig(
        @PathVariable venueId: UUID,
        @RequestBody update: VenueUpdate
    ): ResponseEntity<Venue> {
        val venue = venueService.updateVenue(venueId, update)
        return ResponseEntity.ok(venue)
    }

    @PatchMapping("/{venueId}/move-to-group")
    fun moveVenueToGroup(
        @PathVariable venueId: UUID,
        @RequestBody newVenueGroupId: UUID
    ): ResponseEntity<Venue> {
        val update = VenueUpdate(venueGroupId = newVenueGroupId)
        val venue = venueService.updateVenue(venueId, update)
        return ResponseEntity.ok(venue)
    }

    @DeleteMapping("/{venueId}")
    fun deleteVenue(@PathVariable venueId: UUID): ResponseEntity<Map<String, String>> {
        venueService.deleteVenue(venueId)
        return ResponseEntity.ok(mapOf("message" to "Venue deleted successfully"))
    }
}

