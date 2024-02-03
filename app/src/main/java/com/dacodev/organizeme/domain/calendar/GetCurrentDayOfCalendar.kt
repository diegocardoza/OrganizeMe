package com.dacodev.organizeme.domain.calendar

import org.threeten.bp.LocalDate
import javax.inject.Inject

class GetCurrentDayOfCalendar @Inject constructor(
    private val localDate: LocalDate
) {

    operator fun invoke(): Int {
        val previousYearDate = localDate.minusYears(1)

        return previousYearDate.lengthOfYear() + localDate.dayOfYear-1
    }

}