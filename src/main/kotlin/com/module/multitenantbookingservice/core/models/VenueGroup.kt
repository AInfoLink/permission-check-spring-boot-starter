package com.module.multitenantbookingservice.core.models

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "venue_groups")
class VenueGroup(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    var isDefault: Boolean,

    @JdbcTypeCode(SqlTypes.JSON)
    val annotations: MutableMap<String, Any> = mutableMapOf(),
    @OneToMany(mappedBy = "venueGroup")
    val venues: MutableSet<Venue> = mutableSetOf(),
)