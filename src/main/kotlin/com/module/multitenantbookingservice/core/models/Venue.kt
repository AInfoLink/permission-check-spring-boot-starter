package com.module.multitenantbookingservice.core.models

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*


@Entity
@Table(name = "venues")
class Venue(
    @Id
    val id: UUID = UUID.randomUUID(),
    var name: String,
    var description: String,
    var location: String,
    @JdbcTypeCode(SqlTypes.JSON)
    val annotations: MutableMap<String, String> = mutableMapOf(),
    @ManyToOne
    @JoinColumn(name = "venue_group_id")
    var venueGroup: VenueGroup,

    @ManyToOne
    @JoinColumn(name = "venue_schedule_config_id")
    val scheduleConfig: VenueScheduleConfig
)