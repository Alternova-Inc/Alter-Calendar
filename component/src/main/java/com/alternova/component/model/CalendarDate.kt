package com.alternova.component.model

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class CalendarDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    var isSelectedDay: Boolean,
    val showTitle: Boolean = true,
    val isCurrentMonth: Boolean = true
) {

    fun getLocalDate(): LocalDate = LocalDate.of(year, month, dayOfMonth)
    fun getNameDayOfWeek(): String = getLocalDate().dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale.getDefault())

    fun isEqualsToOtherDate(year: Int, month: Int, dayOfMonth: Int): Boolean =
        this.year == year && this.month == month && this.dayOfMonth == dayOfMonth
}