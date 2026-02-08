package com.module.app.core.service

import com.module.app.core.models.BookingStatus
import com.module.app.core.models.TimeSlotDuration
import com.module.app.core.models.Venue
import com.module.app.core.models.VenueBookingRequest
import com.module.app.core.repository.VenueBookingRequestRepository
import com.module.app.core.repository.VenueRepository
import com.module.app.security.BookingConflict
import com.module.app.security.BookingRequestEmpty
import com.module.app.security.InvalidBookingTime
import com.module.app.security.VenueNotFound
import com.module.app.security.annotation.Permission
import com.module.app.security.annotation.Require
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class BookingRequestFormItem(
    val venueId: UUID,
    val date: LocalDate,
    val hour: Int,
    val price: Int,
    val duration: TimeSlotDuration
)

data class BookingRequestForm(
    val identityId: UUID,
    val items: List<BookingRequestFormItem>,
    val totalPrice: Int
)

interface BookingService {
    fun submitBookingRequest(form: BookingRequestForm)
    fun validateBookingRequest(form: BookingRequestForm): List<Exception>
}

@Service
class DefaultBookingService(
    private val orderService: OrderService,
    private val venueRepository: VenueRepository,
    private val venueBookingRequestRepository: VenueBookingRequestRepository
): BookingService {

    private val logger = LoggerFactory.getLogger(DefaultBookingService::class.java)

    // This method handles the entire booking request submission process, including validation, order creation, and booking request creation,
    // make sure the pricing and order identity are properly set in the form before calling this method,
    // as it assumes the form is already validated for pricing and identity existence.
    // and the form should be fully prepared with correct totalPrice before calling this method
    @Require(Permission.BOOKINGS_CREATE)
    @Transactional
    override fun submitBookingRequest(form: BookingRequestForm) {
        logger.info("Submitting booking request with ${form.items.size} items")

        // Validate the booking request
        val validationErrors = validateBookingRequest(form)
        if (validationErrors.isNotEmpty()) {
            logger.warn("Booking request validation failed with ${validationErrors.size} errors")
            throw validationErrors.first() // Throw the first error encountered
        }

        val venueIds = form.items.map { it.venueId }.distinct()
        val venues = venueRepository.findAllById(venueIds).associateBy { it.id }
        // Check for conflicts with pessimistic locking to prevent race conditions
        checkBookingConflictsWithLock(form)

        // Create OrderItems for each booking
        val orderItems = form.items.map { item ->
            val venue = venues[item.venueId]!!
            OrderItemCreation(
                description = "Booking for ${venue.name} on ${item.date} at ${item.hour}:00",
                amount = item.price
            )
        }

        val order = orderService.createOrder(
            OrderCreation(
                description = "Venue booking for ${form.items.size} time slots",
                amount = form.totalPrice,
                identityId = form.identityId,
                items = orderItems
            )
        )

        // Create VenueBookingRequest for each booking item
        val orderItemsList = order.items.toList()
        val bookingRequests = form.items.mapIndexed { index, item ->
            val venue = venues[item.venueId]!!
            val orderItem = orderItemsList[index]

            VenueBookingRequest(
                venue = venue,
                date = item.date,
                hour = item.hour,
                duration = item.duration,
                status = BookingStatus.PENDING,
                orderItem = orderItem,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }

        // Save all bookings at once for better performance
        venueBookingRequestRepository.saveAll(bookingRequests)

        logger.info("Booking request submitted successfully - Order ID: ${order.id}, ${form.items.size} bookings created")
    }

    @Require(Permission.BOOKINGS_READ)
    @Transactional(readOnly = true)
    override fun validateBookingRequest(form: BookingRequestForm): List<Exception> {
        logger.debug("Validating booking request with ${form.items.size} items")

        if (form.items.isEmpty()) {
            logger.warn("Booking request is empty")
            return listOf(BookingRequestEmpty)
        }

        // Batch load all venues
        val venueIds = form.items.map { it.venueId }.distinct()
        val venues = venueRepository.findAllById(venueIds).associateBy { it.id }

        // Check for missing venues
        val missingVenues = venueIds.filter { it !in venues }
        if (missingVenues.isNotEmpty()) {
            logger.warn("Venues not found: $missingVenues")
            return listOf(VenueNotFound.withDetails("Venues not found: $missingVenues"))
        }

        // Validate each item
        val errors = mutableListOf<Exception>()
        for (item in form.items) {
            val venue = venues[item.venueId]!!

            // Basic validation
            errors.addAll(validateBookingItem(item, venue))

            // Conflict checking
            val optionalConflict = checkConflictsReadOnly(item, venue, item.date)
            if (optionalConflict != null) {
                errors.add(optionalConflict)
            }

        }
        logger.debug("Booking request validation completed with ${errors.size} errors")
        return errors
    }

    /**
     * Check for booking conflicts using pessimistic locking within write transaction
     * to prevent race conditions during booking submission
     */
    private fun checkBookingConflictsWithLock(form: BookingRequestForm) {
        form.items.forEach { item ->
            // Use pessimistic locking to prevent concurrent bookings
            val conflictingBookings = venueBookingRequestRepository.findConflictingBookingsWithLock(
                item.venueId, item.date, item.hour
            )

            if (conflictingBookings.isNotEmpty()) {
                logger.warn("Time slot conflict detected for venue ${item.venueId} on ${item.date} at ${item.hour}:00")
                throw BookingConflict.withDetails("Time slot conflict for venue ${item.venueId} on ${item.date} at ${item.hour}:00")
            }
        }
    }

    /**
     * Validates a single booking item with its venue
     */
    private fun validateBookingItem(item: BookingRequestFormItem, venue: Venue): List<Exception> {
        val errors = mutableListOf<Exception>()

        // Validate hour range
        if (item.hour < 0 || item.hour > 23) {
            logger.warn("Invalid hour: ${item.hour}")
            errors.add(InvalidBookingTime.withDetails("Invalid hour: ${item.hour}"))
        }

        // Validate venue schedule
        if (!venue.scheduleConfig.isActive) {
            logger.warn("Venue ${venue.name} is not available for booking")
            errors.add(InvalidBookingTime.withDetails("Venue ${venue.name} is not available for booking"))
        }
        return errors
    }

    /**
     * Checks for booking conflicts without locking (read-only validation)
     */
    private fun checkConflictsReadOnly(item: BookingRequestFormItem, venue: Venue, bookingDate: LocalDate): Exception? {
        val existingBookings = venueBookingRequestRepository.findByVenueAndDateAndHour(venue, bookingDate, item.hour)
        val activeBookings = existingBookings.filter {
            it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.PENDING
        }

        return if (activeBookings.isNotEmpty()) {
            logger.warn("Time slot conflict for venue ${venue.name} on $bookingDate at ${item.hour}:00")
            BookingConflict.withDetails("Time slot conflict for venue ${venue.name} on $bookingDate at ${item.hour}:00")
        } else null
    }

}