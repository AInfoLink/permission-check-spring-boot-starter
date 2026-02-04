package com.module.app.core.repository

import com.module.app.core.models.VenueBookingRequest
import com.module.app.core.models.BookingStatus
import com.module.app.core.models.Venue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface VenueBookingRequestRepository : JpaRepository<VenueBookingRequest, UUID> {

    fun findByVenueAndDateAndHour(venue: Venue, date: LocalDate, hour: Int): List<VenueBookingRequest>

    fun findByVenueAndDateBetween(venue: Venue, startDate: LocalDate, endDate: LocalDate): List<VenueBookingRequest>

    fun findByStatus(status: BookingStatus): List<VenueBookingRequest>

    @Query("SELECT br FROM VenueBookingRequest br WHERE br.venue = :venue AND br.date = :date AND br.status = :status")
    fun findByVenueAndDateAndStatus(
        @Param("venue") venue: Venue,
        @Param("date") date: LocalDate,
        @Param("status") status: BookingStatus
    ): List<VenueBookingRequest>
}