package com.module.multitenantbookingservice.core.models

import com.module.multitenantbookingservice.security.InvalidSlotDuration
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "venue_schedule_configs")
class VenueScheduleConfig(
    @Id
    val id: UUID = UUID.randomUUID(),

    val name: String,
    private var slotDurationMinutes: Int,
    val isActive: Boolean = true,
    @JdbcTypeCode(SqlTypes.JSON)
    val annotation: MutableMap<String, Any> = mutableMapOf(),
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "config")
    val venues: List<Venue> = mutableListOf(),
) {
    private fun isValidSlotDuration(duration: Int): Boolean {
        // 確保每小時可以被 slotDurationMinutes 整除
        return 60 % duration == 0 && duration > 0 && duration <= 60
    }

    fun getSlotDurationMinutes(): Int {
        return slotDurationMinutes
    }

    fun setSlotDurationMinutes(duration: Int) {
        if (!isValidSlotDuration(duration)) {
            throw InvalidSlotDuration.withDetails("Duration must be a positive divisor of 60.")
        }
        slotDurationMinutes = duration
    }
}