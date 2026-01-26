package com.module.multitenantbookingservice.core.web.controller

import com.module.multitenantbookingservice.core.models.Venue
import com.module.multitenantbookingservice.core.models.BookingSlotType
import com.module.multitenantbookingservice.core.service.VenueCreation
import com.module.multitenantbookingservice.core.service.VenueService
import com.module.multitenantbookingservice.core.service.VenueUpdate
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

    @GetMapping("/by-name/{name}")
    fun getVenueByName(@PathVariable name: String): ResponseEntity<Venue> {
        val venue = venueService.getVenueByName(name)
        return ResponseEntity.ok(venue)
    }

    @GetMapping("/by-group/{venueGroupId}")
    fun getVenuesByGroup(@PathVariable venueGroupId: UUID): ResponseEntity<List<Venue>> {
        val venues = venueService.getVenuesByGroup(venueGroupId)
        return ResponseEntity.ok(venues)
    }

    @GetMapping
    fun getAllVenues(): ResponseEntity<List<Venue>> {
        val venues = venueService.getAllVenues()
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

