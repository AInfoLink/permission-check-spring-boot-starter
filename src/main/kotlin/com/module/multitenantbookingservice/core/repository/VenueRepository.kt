package com.module.multitenantbookingservice.core.repository

import com.module.multitenantbookingservice.core.models.Venue
import com.module.multitenantbookingservice.core.models.VenueGroup
import org.springframework.data.repository.ListCrudRepository
import java.util.*

interface VenueRepository: ListCrudRepository<Venue, UUID> {
    fun findByName(name: String): Optional<Venue>
    fun findByVenueGroup(venueGroup: VenueGroup): List<Venue>
    fun findByVenueGroupId(venueGroupId: UUID): List<Venue>
}