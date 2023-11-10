package com.alternova.component.model

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class CalendarDate(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    internal val controller: CalendarController,
) {

    fun getLocalDate(): LocalDate = LocalDate.of(year, month, dayOfMonth)
    internal fun getNameDayOfWeek(): String = getLocalDate().dayOfWeek
        .getDisplayName(TextStyle.SHORT, Locale.getDefault())

    internal fun getNameMont(): String = getLocalDate().month
        .getDisplayName(TextStyle.FULL, Locale.getDefault())

    internal fun isEqualsToOtherDate(year: Int, month: Int, dayOfMonth: Int): Boolean =
        this.year == year && this.month == month && this.dayOfMonth == dayOfMonth
}