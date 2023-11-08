package com.alternova.component.complex.model

import com.alternova.component.model.CalendarDate
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

internal class ComplexCalendarDate(
    val date:LocalDate,
    val days: List<CalendarDate>
){
    fun getMonthName(): String {
        return date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
}