package com.dacodev.organizeme.domain.calendar.model

import com.dacodev.organizeme.ui.adapters.model.CalendarDayForRecycler

data class CalendarDay(
    val year: Int = 0,
    val month:Int = 0,
    val dayOfMonth: Int = 0,
    val dayOfWeek: Int = 0
)

fun CalendarDay.toCalendarDayForRecycler():CalendarDayForRecycler =
    CalendarDayForRecycler(year, month, dayOfMonth, dayOfWeek,false)
