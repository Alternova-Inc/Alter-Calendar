package com.alternova.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.alternova.component.common.ControllerListener
import com.alternova.component.complex.CalendarComplexAdapter
import com.alternova.component.complex.ComplexCalendarComponent
import com.alternova.component.complex.model.ComplexCalendarDate
import com.alternova.component.model.CalendarController
import com.alternova.component.model.CalendarDate
import com.alternova.component.single.SingleCalendarComponent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class CalendarView(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), ControllerListener {
    private var selectedDay: CalendarDate? = null
    private var calendarListener: CalendarListener? = null
    private val titleCalendar: TextView by lazy { findViewById(R.id.titleCalendar) }
    private val startDayOfWeek = startInLastSunday()
    private var initialDate: LocalDate? = null
    private var monthNameToShow = getMonthNameToShow()
    private var currentArrowIcon: Int = R.drawable.ic_arrow_down

    private var showFutureDays: Boolean = true
    private var showPastDays: Boolean = true

    private lateinit var singleCalendar: SingleCalendarComponent
    private lateinit var complexCalendar: ComplexCalendarComponent

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarView, 0, 0)
            .apply {
                try {
                    showFutureDays = getBoolean(R.styleable.CalendarView_show_futureDays, true)
                    showPastDays = getBoolean(
                        R.styleable.CalendarView_show_daysPriorToTheStartOfTheCalendar,
                        true
                    )
                } finally {
                    recycle()
                }
            }
        LayoutInflater.from(context).inflate(R.layout.view_calendar, this, true)
        initView()
    }

    fun setStartCalendar(initialDate: LocalDate) {
        this.initialDate = initialDate
        initSingleCalendar()
    }

    fun updateDateCalendar(localDate: LocalDate) {
        val date = CalendarDate.setLocalDate(localDate)
        selectedDay(date)
    }

    private fun startInLastSunday(): LocalDate {
        val currentDate = LocalDate.now()
        val currentDayOfWeek = currentDate.dayOfWeek.value
        return currentDate.minusDays(currentDayOfWeek.toLong())
    }

    private fun updateMonthName(monthNameToShow: String) {
        if (monthNameToShow != this@CalendarView.monthNameToShow) {
            this@CalendarView.monthNameToShow = monthNameToShow
            putMonthName()
        }
    }

    private fun initView() {
        initSingleCalendar()
        initComplexCalendar()
        initTitleCalendar()
    }

    private fun getMonthNameToShow(pivotDate: LocalDate = startDayOfWeek) =
        pivotDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    private fun initSingleCalendar() {
        singleCalendar = findViewById(R.id.singleCalendar)
        singleCalendar.setInitCalendarDate(initialDate)
        singleCalendar.addOnControllerListener(this)
    }

    private fun initTitleCalendar() {
        putMonthName()
        titleCalendar.setOnClickListener {
            if (currentArrowIcon == R.drawable.ic_arrow_down) {
                currentArrowIcon = R.drawable.ic_arrow_up
                singleCalendar.visibility = GONE
                complexCalendar.visibility = VISIBLE
            } else {
                currentArrowIcon = R.drawable.ic_arrow_down
                singleCalendar.visibility = VISIBLE
                complexCalendar.visibility = GONE
            }
            titleCalendar.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, currentArrowIcon, 0)
        }
    }

    private fun putMonthName() {
        titleCalendar.text = monthNameToShow
    }

    fun addOnCalendarListener(calendarListener: CalendarListener) {
        this.calendarListener = calendarListener
        singleCalendar.addOnCalendarListener(calendarListener)
        complexCalendar.addOnCalendarListener(calendarListener)
    }

    private fun initComplexCalendar() {
        complexCalendar = findViewById(R.id.complexCalendar)
        complexCalendar.setInitCalendarDate(initialDate)
        complexCalendar.addOnControllerListener(this)
    }

    override fun changeMonth(nameMonth: String) {
        updateMonthName(nameMonth)
    }

    override fun selectedDay(date: CalendarDate) {
        this.selectedDay = date
        singleCalendar.updateSelectedDay(date)
        complexCalendar.updateSelectedDay(date)
    }

}