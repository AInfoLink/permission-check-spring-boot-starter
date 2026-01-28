package com.module.multitenantbookingservice.core.models

import com.module.multitenantbookingservice.core.repository.VenueTimeSlotConfigRepository
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class VenueTimeSlotConfigValidator {

    @Autowired
    private lateinit var repository: VenueTimeSlotConfigRepository

    @PrePersist
    @PreUpdate
    fun validateTimeSlotOverlap(config: VenueTimeSlotConfig) {
        validateTimeRange(config)
        validateNoOverlap(config)
    }

    private fun validateTimeRange(config: VenueTimeSlotConfig) {
        if (config.startTime >= config.endTime) {
            throw IllegalArgumentException("End time must be greater than start time")
        }
    }

    private fun validateNoOverlap(config: VenueTimeSlotConfig) {
        val existingConfigs = repository.findByVenue(config.venue)

        existingConfigs.forEach { existing ->
            if (existing.id != config.id && isTimeOverlap(config, existing)) {
                throw IllegalArgumentException(
                    "Time slot overlap: ${config.startTime}-${config.endTime} " +
                    "overlaps with existing config ${existing.startTime}-${existing.endTime}"
                )
            }
        }
    }

    /**
     * Check if two time slot configurations overlap
     *
     * Example:
     * A: [10:00 -------- 12:00)
     * B:       [11:00 -------- 13:00) -> Overlap
     */
    private fun isTimeOverlap(first: VenueTimeSlotConfig, second: VenueTimeSlotConfig): Boolean {
        val firstStartsBeforeSecondEnds = first.startTime < second.endTime
        val firstEndsAfterSecondStarts = first.endTime > second.startTime
        return firstStartsBeforeSecondEnds && firstEndsAfterSecondStarts
    }
}