package com.alternova.component.model

data class CalendarController(
    internal var isSelectedDay: Boolean,
    internal val showTitle: Boolean = true,
    internal val isCurrentMonth: Boolean = true,
    internal var dayEnabled: Boolean = true
)
