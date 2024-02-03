package com.dacodev.organizeme.domain.calendar

import com.dacodev.organizeme.domain.calendar.model.CalendarDay
import org.threeten.bp.LocalDate
import javax.inject.Inject

class GetCalendarDays @Inject constructor(
    private val localDate: LocalDate
) {

    operator fun invoke(): List<CalendarDay> {
        val calendarDays = ArrayList<CalendarDay>()

        val daysOfCurrentYearList = ArrayList<CalendarDay>()
        val currentYear = localDate.year
        val firstDayOfYear = LocalDate.ofYearDay(currentYear, 1)
        var currentDay = firstDayOfYear
        while (currentDay.year == currentYear) {
            daysOfCurrentYearList.add(
                CalendarDay(
                    currentDay.year,
                    currentDay.monthValue,
                    currentDay.dayOfMonth,
                    currentDay.dayOfWeek.value
                )
            )
            currentDay = currentDay.plusDays(1)
        }

        val previousYearDate = localDate.minusYears(1)
        val daysOfPreviousYearList = ArrayList<CalendarDay>()
        val previousYear = previousYearDate.year
        val firstDayOfPreviousYear = LocalDate.ofYearDay(previousYear, 1)
        var indexOfPreviousYear = firstDayOfPreviousYear
        while (indexOfPreviousYear.year == previousYear) {
            daysOfPreviousYearList.add(
                CalendarDay(
                    indexOfPreviousYear.year,
                    indexOfPreviousYear.monthValue,
                    indexOfPreviousYear.dayOfMonth,
                    indexOfPreviousYear.dayOfWeek.value
                )
            )
            indexOfPreviousYear = indexOfPreviousYear.plusDays(1)
        }

        val nextYearDate = localDate.plusYears(1)
        val daysOfNextYearList = ArrayList<CalendarDay>()
        val nextYear = nextYearDate.year
        val firstDayOfNextYear = LocalDate.ofYearDay(nextYear, 1)
        var indexOfNextYear = firstDayOfNextYear
        while (indexOfNextYear.year == nextYear) {
            daysOfNextYearList.add(
                CalendarDay(
                    indexOfNextYear.year,
                    indexOfNextYear.monthValue,
                    indexOfNextYear.dayOfMonth,
                    indexOfNextYear.dayOfWeek.value
                )
            )
            indexOfNextYear = indexOfNextYear.plusDays(1)
        }

        calendarDays.addAll(daysOfPreviousYearList)
        calendarDays.addAll(daysOfCurrentYearList)
        calendarDays.addAll(daysOfNextYearList)

        return calendarDays
    }
}