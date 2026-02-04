package com.module.app.core.repository

import com.module.app.core.models.VenueGroup
import org.springframework.data.repository.ListCrudRepository
import java.util.*

interface VenueGroupRepository: ListCrudRepository<VenueGroup, UUID> {
    fun findByName(name: String): Optional<VenueGroup>
    fun findByIsDefaultTrue(): Optional<VenueGroup>
}