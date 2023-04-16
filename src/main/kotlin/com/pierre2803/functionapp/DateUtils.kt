package com.pierre2803.functionapp

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

object DateUtils {

    fun findPreviousWeekdayOccurrence(datetime: ZonedDateTime, toDayOfWeek: DayOfWeek, toLocalTime: LocalTime): ZonedDateTime = datetime.with(TemporalAdjusters.previousOrSame(toDayOfWeek)).with(toLocalTime)

    fun findNextWeekdayOccurrence(datetime: ZonedDateTime, toDayOfWeek: DayOfWeek, toLocalTime: LocalTime): ZonedDateTime {
        val localTime = datetime.toLocalTime()

        return when {
            datetime.dayOfWeek == toDayOfWeek && localTime < toLocalTime -> datetime.with(toLocalTime)
            datetime.dayOfWeek == toDayOfWeek && localTime >= toLocalTime -> datetime.with(TemporalAdjusters.next(toDayOfWeek)).with(toLocalTime)
            else -> datetime.with(TemporalAdjusters.nextOrSame(toDayOfWeek)).with(toLocalTime)
        }
    }
}