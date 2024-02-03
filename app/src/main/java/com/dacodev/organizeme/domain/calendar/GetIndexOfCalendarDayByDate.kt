package com.dacodev.organizeme.domain.calendar

import org.threeten.bp.LocalDate
import javax.inject.Inject

class GetIndexOfCalendarDayByDate @Inject constructor(
    private val localDate: LocalDate
) {

    operator fun invoke(date:String): Int {
        val previousYearDate = localDate.minusYears(1)

        val datePerParts = date.split("-")

        val year = datePerParts[0].toInt()
        val month = datePerParts[1].toInt()
        val day = datePerParts[2].toInt()
        val currentDate = LocalDate.of(year,month,day)
        val indexCurrentDay = currentDate.dayOfYear-1

        return previousYearDate.lengthOfYear() + indexCurrentDay
    }

}