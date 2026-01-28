package com.module.multitenantbookingservice.core.booking.strategy

import java.time.LocalTime

/**
 * Time range using [start, end) semantics
 * - start: inclusive
 * - end: exclusive
 */
data class TimeRange(
    val start: LocalTime,
    val end: LocalTime
) {
    /**
     * Check if this time range spans across midnight
     * 23:00 -> 01:00 = overnight
     * 10:00 -> 12:00 = same day
     */
    private fun isOvernight(): Boolean {
        return start >= end
    }

    /**
     * Split overnight time range into non-overnight segments
     *
     * Same day: [10:00, 12:00) -> [[10:00, 12:00)]
     * Overnight: [23:00, 01:00) -> [[23:00, 24:00), [00:00, 01:00)]
     */
    fun expandOvernight(): List<TimeRange> {
        if (!isOvernight()) {
            // Same day, return as-is
            return listOf(this)
        }

        // Overnight, split into two segments
        val firstPart = TimeRange(
            start = this.start,
            end = LocalTime.MAX // represents 24:00
        )

        val secondPart = TimeRange(
            start = LocalTime.MIN, // represents 00:00
            end = this.end
        )

        return listOf(firstPart, secondPart)
    }

    /**
     * Check if two [non-overnight] time ranges overlap
     *
     * Overlap condition: A hasn't ended yet AND B has already started
     */
    private fun isOverlapWith(other: TimeRange): Boolean {
        val thisStartsBeforeOtherEnds = this.start < other.end
        val thisEndsAfterOtherStarts = this.end > other.start

        return thisStartsBeforeOtherEnds && thisEndsAfterOtherStarts
    }

    /**
     * Check if two time ranges overlap (supports overnight)
     *
     * Steps:
     * 1. Split this into 1~2 segments
     * 2. Split other into 1~2 segments
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
}