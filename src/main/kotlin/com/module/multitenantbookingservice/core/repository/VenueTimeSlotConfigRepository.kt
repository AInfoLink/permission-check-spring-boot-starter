package com.module.multitenantbookingservice.core.repository

import com.module.multitenantbookingservice.core.models.Venue
import com.module.multitenantbookingservice.core.models.VenueTimeSlotConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalTime
import java.util.*

@Repository
interface VenueTimeSlotConfigRepository : JpaRepository<VenueTimeSlotConfig, UUID> {

    // 查找适用的时间段配置
    @Query("""
        SELECT c FROM VenueTimeSlotConfig c
        WHERE c.venue = :venue
        AND c.startTime <= :bookingTime
        AND c.endTime > :bookingTime
    """)
    fun findByVenueAndTime(
        @Param("venue") venue: Venue,
        @Param("bookingTime") bookingTime: LocalTime
    ): VenueTimeSlotConfig?

    // 查找所有场地的时间段配置 (用于验证重叠)
    fun findByVenue(venue: Venue): List<VenueTimeSlotConfig>
}