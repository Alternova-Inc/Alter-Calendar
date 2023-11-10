package com.alternova.component.common

import com.alternova.component.model.CalendarDate

internal interface ControllerListener {
    fun changeMonth(nameMonth: String)
    fun selectedDay(date: CalendarDate)
}