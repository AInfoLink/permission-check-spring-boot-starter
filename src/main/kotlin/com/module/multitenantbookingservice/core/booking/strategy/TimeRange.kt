package com.module.multitenantbookingservice.core.booking.strategy

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Time range using [start, end) semantics with date and time support
 * - start: inclusive
 * - end: exclusive
 */
data class TimeRange(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
) {

    val duration: Duration = Duration.between(startTime, endTime)

    /**
     * Convenience constructor for same-day time ranges
     */
    constructor(date: LocalDate, startTime: LocalTime, endTime: LocalTime) : this(
        startTime = date.atTime(startTime),
        endTime = if (startTime <= endTime) {
            date.atTime(endTime)
        } else {
            // If startTime > endTime, assume it spans to next day
            date.plusDays(1).atTime(endTime)
        }
    )

    /**
     * Convenience constructor for time ranges within a single day
     */
    constructor(startTime: LocalTime, endTime: LocalTime) : this(
        LocalDate.now(), startTime, endTime
    )
    /**
     * Check if this time range spans across multiple days
     */
    private fun isMultiDay(): Boolean {
        return startTime.toLocalDate() != endTime.toLocalDate()
    }

    /**
     * Check if this time range spans across midnight within the same calendar transition
     * (for backward compatibility with overnight logic)
     */
    private fun isOvernight(): Boolean {
        return isMultiDay() &&
               Duration.between(startTime, endTime).toDays() == 1L &&
               startTime.toLocalTime() > endTime.toLocalTime()
    }

    /**
     * Split multi-day time range into daily segments
     *
     * Same day: [2024-01-01 10:00, 2024-01-01 12:00) -> [[2024-01-01 10:00, 2024-01-01 12:00)]
     * Multi-day: [2024-01-01 23:00, 2024-01-02 01:00) -> [[2024-01-01 23:00, 2024-01-02 00:00), [2024-01-02 00:00, 2024-01-02 01:00)]
     */
    fun expandOvernight(): List<TimeRange> {
        if (!isMultiDay()) {
            // Same day, return as-is
            return listOf(this)
        }

        val segments = mutableListOf<TimeRange>()
        var currentStart = startTime
        val endDate = endTime.toLocalDate()

        while (currentStart.toLocalDate() < endDate) {
            val currentEndOfDay = currentStart.toLocalDate().atTime(23, 59, 59, 999_999_999)
            val nextDayStart = currentStart.toLocalDate().plusDays(1).atStartOfDay()

            // Add segment for current day (from currentStart to end of day)
            segments.add(TimeRange(currentStart, nextDayStart))

            // Move to next day
            currentStart = nextDayStart
        }

        // Add final segment if there's remaining time on the last day
        if (currentStart < endTime) {
            segments.add(TimeRange(currentStart, endTime))
        }

        return segments
    }

    /**
     * Check if two time ranges overlap
     *
     * Overlap condition: A hasn't ended yet AND B has already started
     */
    private fun isOverlapWith(other: TimeRange): Boolean {
        val thisStartsBeforeOtherEnds = this.startTime.isBefore(other.endTime)
        val thisEndsAfterOtherStarts = this.endTime.isAfter(other.startTime)

        return thisStartsBeforeOtherEnds && thisEndsAfterOtherStarts
    }

    /**
     * Check if two time ranges overlap (supports multi-day ranges)
     *
     * Steps:
     * 1. Split this into daily segments if needed
     * 2. Split other into daily segments if needed
     * 3. If any segment overlaps, consider as overlapping
     */
    fun isOverlapWithAllowOvernight(other: TimeRange): Boolean {
        val thisRanges = this.expandOvernight()
        val otherRanges = other.expandOvernight()

        for (thisRange in thisRanges) {
            for (otherRange in otherRanges) {
                if (thisRange.isOverlapWith(otherRange)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Simple overlap check for datetime ranges
     */
    fun overlapsWith(other: TimeRange): Boolean {
        return this.isOverlapWith(other)
    }

    /**
     * Get all dates covered by this time range
     */
    fun getCoveredDates(): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = startTime.toLocalDate()
        val endDate = endTime.toLocalDate()

        while (currentDate <= endDate) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        return dates
    }

    /**
     * Check if this time range contains a specific datetime
     */
    fun contains(dateTime: LocalDateTime): Boolean {
        return !dateTime.isBefore(startTime) && dateTime.isBefore(endTime)
    }
}