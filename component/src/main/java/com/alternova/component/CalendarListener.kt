package com.alternova.component

import com.alternova.component.model.CalendarDate

interface CalendarListener {
    fun onSelectedDate(date: CalendarDate)
}