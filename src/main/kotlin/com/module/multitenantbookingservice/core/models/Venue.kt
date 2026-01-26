package com.module.multitenantbookingservice.core.models

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*


@Entity
@Table(name = "venues")
class Venue(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "description", nullable = false, length = 1000)
    var description: String,

    @Column(name = "location", nullable = false, length = 500)
    var location: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "annotations", nullable = false)
    val annotations: MutableMap<String, Any> = mutableMapOf(),

    @ManyToOne
    @JoinColumn(name = "venue_group_id", nullable = false)
    var venueGroup: VenueGroup,

    @Embedded
    var scheduleConfig: VenueScheduleConfig = VenueScheduleConfig()
)