package com.alternova.component.single

import com.alternova.component.model.CalendarDate

internal interface SingleControllerListener {
    fun changeMonth(nameMonth: String)
    fun selectedDay(date: CalendarDate)
}