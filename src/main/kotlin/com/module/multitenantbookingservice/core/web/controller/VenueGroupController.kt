package com.module.multitenantbookingservice.core.web.controller

import com.module.multitenantbookingservice.core.models.VenueGroup
import com.module.multitenantbookingservice.core.service.VenueGroupCreation
import com.module.multitenantbookingservice.core.service.VenueGroupUpdate
import com.module.multitenantbookingservice.core.service.VenueService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/venue-groups")
class VenueGroupController(
    private val venueService: VenueService
) {

    @PostMapping
    fun createVenueGroup(@RequestBody request: VenueGroupCreation): ResponseEntity<VenueGroup> {
        val venueGroup = venueService.createVenueGroup(request)
        return ResponseEntity(venueGroup, HttpStatus.CREATED)
    }

    @GetMapping("/{venueGroupId}")
    fun getVenueGroup(@PathVariable venueGroupId: UUID): ResponseEntity<VenueGroup> {
        val venueGroup = venueService.getVenueGroup(venueGroupId)
        return ResponseEntity.ok(venueGroup)
    }

    @GetMapping("/by-name/{name}")
    fun getVenueGroupByName(@PathVariable name: String): ResponseEntity<VenueGroup> {
        val venueGroup = venueService.getVenueGroupByName(name)
        return ResponseEntity.ok(venueGroup)
    }

    @GetMapping("/default")
    fun getDefaultVenueGroup(): ResponseEntity<VenueGroup> {
        val venueGroup = venueService.getDefaultVenueGroup()
        return ResponseEntity.ok(venueGroup)
    }

    @GetMapping
    fun getAllVenueGroups(): ResponseEntity<List<VenueGroup>> {
        val venueGroups = venueService.getAllVenueGroups()
        return ResponseEntity.ok(venueGroups)
    }

    @PutMapping("/{venueGroupId}")
    fun updateVenueGroup(
        @PathVariable venueGroupId: UUID,
        @RequestBody update: VenueGroupUpdate
    ): ResponseEntity<VenueGroup> {
        val venueGroup = venueService.updateVenueGroup(venueGroupId, update)
        return ResponseEntity.ok(venueGroup)
    }

    @DeleteMapping("/{venueGroupId}")
    fun deleteVenueGroup(@PathVariable venueGroupId: UUID): ResponseEntity<Map<String, String>> {
        venueService.deleteVenueGroup(venueGroupId)
        return ResponseEntity.ok(mapOf("message" to "Venue group deleted successfully"))
    }

    @PatchMapping("/{venueGroupId}/set-default")
    fun setAsDefaultVenueGroup(@PathVariable venueGroupId: UUID): ResponseEntity<VenueGroup> {
        val venueGroup = venueService.updateVenueGroup(
            venueGroupId,
            VenueGroupUpdate(isDefault = true)
        )
        return ResponseEntity.ok(venueGroup)
    }

    @PatchMapping("/{venueGroupId}/annotations")
    fun updateVenueGroupAnnotations(
        @PathVariable venueGroupId: UUID,
        @RequestBody request: AnnotationsUpdateRequest
    ): ResponseEntity<VenueGroup> {
        val venueGroup = venueService.updateVenueGroup(
            venueGroupId,
            VenueGroupUpdate(annotations = request.annotations)
        )
        return ResponseEntity.ok(venueGroup)
    }
}

data class AnnotationsUpdateRequest(
    val annotations: MutableMap<String, Any>
)