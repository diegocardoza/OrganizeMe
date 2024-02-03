package com.dacodev.organizeme.ui.adapters.model

data class CalendarDayForRecycler(
    val year: Int = 0,
    val month:Int = 0,
    val dayOfMonth: Int = 0,
    val dayOfWeek: Int = 0,
    var isSelected:Boolean = false
)