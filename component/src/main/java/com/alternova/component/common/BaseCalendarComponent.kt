package com.alternova.component.common

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.alternova.component.CalendarListener
import com.alternova.component.model.CalendarDate
import java.time.DayOfWeek
import java.time.LocalDate

internal abstract class BaseCalendarComponent(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {

    protected val onItemClick: (CalendarDate) -> Unit = {
        updateSelectedDay(it)
        calendarListener?.onSelectedDate(it)
        controllerListener?.selectedDay(it)
    }

    protected var controllerListener: ControllerListener? = null
    protected var calendarListener: CalendarListener? = null
    protected var initDate: LocalDate? = null

    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun addListener()
    protected abstract fun notifyAdapter(onMoveToPosition: () -> Unit)

    fun addOnCalendarListener(calendarListener: CalendarListener) {
        this.calendarListener = calendarListener
    }

    fun addOnControllerListener(controllerListener: ControllerListener) {
        this.controllerListener = controllerListener
    }

    abstract fun setInitCalendarDate(initDate: LocalDate?)
    abstract fun updateSelectedDay(selectedDay: CalendarDate)
}
fun DayOfWeek.getDayOfWeek(): Int = when (this) {
    DayOfWeek.SUNDAY -> 0
    DayOfWeek.MONDAY -> 1
    DayOfWeek.TUESDAY -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY -> 4
    DayOfWeek.FRIDAY -> 5
    DayOfWeek.SATURDAY -> 6
}