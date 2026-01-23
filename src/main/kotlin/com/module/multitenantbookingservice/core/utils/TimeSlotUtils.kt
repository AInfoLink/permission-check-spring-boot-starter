package com.module.multitenantbookingservice.core.utils

import java.time.LocalDateTime


object TimeSlotUtils {
    /**
     * 將給定的日期時間對齊到標準的 15 分鐘時間槽。
     * 如果 roundUp 為 true，則向上對齊到下一個時間槽；否則向下對齊。
     *
     * 例如：
     * - 10:07 -> 10:00 (roundUp = false)
     * - 10:07 -> 10:15 (roundUp = true)
     * - 10:15 -> 10:15 (roundUp = false)
     * - 10:15 -> 10:15 (roundUp = true)
     * - 10:59 -> 10:45 (roundUp = false)
     * - 10:59 -> 11:00 (roundUp = true)
     */
    private const val STANDARD_TIME_SLOT_MINUTES = 15

    fun alignToStandardTimeSlot(dateTime: LocalDateTime, roundUp: Boolean = false): LocalDateTime {
        val minutes = dateTime.minute
        val alignedMinutes = if (roundUp) {
            ((minutes / STANDARD_TIME_SLOT_MINUTES) + 1) * STANDARD_TIME_SLOT_MINUTES
        } else {
            (minutes / 15) * STANDARD_TIME_SLOT_MINUTES
        }

        return if (alignedMinutes >= 60) {
            dateTime.plusHours(1).withMinute(alignedMinutes - 60).withSecond(0).withNano(0)
        } else {
            dateTime.withMinute(alignedMinutes).withSecond(0).withNano(0)
        }
    }

    fun generateTimeSlots(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<LocalDateTime> {
        val slots = mutableListOf<LocalDateTime>()
        val alignedStartTime = alignToStandardTimeSlot(startTime, roundUp = false)
        var currentTime = alignedStartTime
        while (currentTime.isBefore(endTime)) {
            slots.add(currentTime)
            currentTime = currentTime.plusMinutes(STANDARD_TIME_SLOT_MINUTES.toLong())
        }
        return slots
    }
}