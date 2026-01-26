package com.module.multitenantbookingservice.core.models

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "venue_groups")
class VenueGroup(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "description", nullable = false, length = 1000)
    var description: String,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "annotations", nullable = false)
    val annotations: MutableMap<String, Any> = mutableMapOf(),

    @OneToMany(mappedBy = "venueGroup")
    val venues: MutableSet<Venue> = mutableSetOf()
)