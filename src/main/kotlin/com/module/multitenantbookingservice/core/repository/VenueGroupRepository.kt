package com.module.multitenantbookingservice.core.repository

import com.module.multitenantbookingservice.core.models.VenueGroup
import org.springframework.data.repository.ListCrudRepository
import java.util.*

interface VenueGroupRepository: ListCrudRepository<VenueGroup, UUID> {
    fun findByName(name: String): Optional<VenueGroup>
    fun findByIsDefaultTrue(): Optional<VenueGroup>
}