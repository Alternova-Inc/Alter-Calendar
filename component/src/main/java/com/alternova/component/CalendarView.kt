package com.alternova.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.alternova.component.complex.CalendarComplexAdapter
import com.alternova.component.complex.model.ComplexCalendarDate
import com.alternova.component.model.CalendarController
import com.alternova.component.model.CalendarDate
import com.alternova.component.single.SingleCalendarComponent
import com.alternova.component.single.SingleControllerListener
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class CalendarView(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs), SingleControllerListener {
    private var selectedDay: CalendarDate? = null
    private var calendarListener: CalendarListener? = null
    private val calendarRecycler by lazy { findViewById<SingleCalendarComponent>(R.id.singleCalendar) }
    private val titleCalendar: TextView by lazy { findViewById(R.id.titleCalendar) }
    private val startDayOfWeek = startInLastSunday()
    private var initialDate: LocalDate? = null
    private var monthNameToShow = getMonthNameToShow()
    private var currentArrowIcon: Int = R.drawable.ic_arrow_down

    private var isFirsTime = true
    private var showFutureDays: Boolean = true
    private var showPastDays: Boolean = true

    private val complexCalendarDates: MutableList<ComplexCalendarDate> = mutableListOf()
    private val onComplexAction: (CalendarDate) -> Unit = {
        updateComplexCalendar(it)
        updateMonthName(it.getLocalDate())
        calendarListener?.onSelectedDate(it)
        selectedDay = it
    }
    private val calendarComplexAdapter by lazy {
        CalendarComplexAdapter(mutableListOf(), onComplexAction)
    }
    private val calendarPager by lazy { findViewById<ViewPager2>(R.id.bodyComplexCalendar) }

    //    TODO: This is a new implementation
    private lateinit var singleCalendar: SingleCalendarComponent

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
    }

    private fun startInLastSunday(): LocalDate {
        val currentDate = LocalDate.now()
        val currentDayOfWeek = currentDate.dayOfWeek.value
        return currentDate.minusDays(currentDayOfWeek.toLong())
    }

    private fun updateMonthName(pivotDate: LocalDate) {
        val monthNameToShow = getMonthNameToShow(pivotDate)
        updateMonthName(monthNameToShow)
    }

    private fun updateMonthName(monthNameToShow: String) {
        if (monthNameToShow != this@CalendarView.monthNameToShow) {
            this@CalendarView.monthNameToShow = monthNameToShow
            putMonthName()
        }
    }

    private fun updateComplexCalendar(date: CalendarDate) {
        complexCalendarDates.forEach { parent ->
            parent.days.forEach { child -> child.controller.isSelectedDay = false }
            parent.days.find { child ->
                child.isEqualsToOtherDate(date.year, date.month, date.dayOfMonth)
            }?.controller?.isSelectedDay = true
        }
        calendarComplexAdapter.update(complexCalendarDates)
    }

    private fun initView() {
        initSingleCalendar()
        initComplexCalendar()
        initTitleCalendar()
    }

    private fun getMonthNameToShow(pivotDate: LocalDate = startDayOfWeek) =
        pivotDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    private fun isSelectedDay(pivotDate: LocalDate): Boolean = LocalDate.now().let {
        pivotDate.dayOfMonth == it.dayOfMonth &&
                pivotDate.monthValue == it.monthValue &&
                pivotDate.year == it.year
    }

    private fun initSingleCalendar() {
        singleCalendar = findViewById(R.id.singleCalendar)
        singleCalendar.setInitCalendarDate(LocalDate.now().minusDays(80))
        singleCalendar.addOnControllerListener(this)
    }

    private fun initTitleCalendar() {
        putMonthName()
        titleCalendar.setOnClickListener {
            if (currentArrowIcon == R.drawable.ic_arrow_down) {
                currentArrowIcon = R.drawable.ic_arrow_up
                calendarRecycler.visibility = GONE
                calendarPager.visibility = VISIBLE
            } else {
                currentArrowIcon = R.drawable.ic_arrow_down
                calendarRecycler.visibility = VISIBLE
                calendarPager.visibility = GONE
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
    }

    private fun initComplexCalendar() {
        calendarPager.adapter = calendarComplexAdapter
        addCalendarDates()
        calendarPager.setCurrentItem(1, false)
        addListenerCalendarPager()
    }

    private fun addCalendarDates(startMonth: LocalDate = startDayOfWeek, addInit: Boolean = false) {
        buildMonth(startMonth, addInit)
        if (isFirsTime) {
            isFirsTime = false
            buildMonth(startMonth.minusMonths(1), addInit = true)
            buildMonth(startMonth.plusMonths(1), addInit = false)
        }
        calendarComplexAdapter.update(complexCalendarDates)
    }

    private fun getDayOfWeek(day: DayOfWeek): Int =
        when (day) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }

    private fun buildMonth(currentMonth: LocalDate, addInit: Boolean) {
        val dates = mutableListOf<CalendarDate>()
        val firstDayMonth = currentMonth.minusDays(currentMonth.dayOfMonth.toLong() - 1)
        initialDate?.apply {
            if (firstDayMonth.isBefore(this)) {
                return
            }
        }
        val firstOfWeekMonth = firstDayMonth.minusDays(
            getDayOfWeek(firstDayMonth.dayOfWeek).toLong()
        )
        val valueMonth = currentMonth.month.value
        repeat(42) {
            val day = firstOfWeekMonth.plusDays(it.toLong())
            val controller = CalendarController(
                isSelectedDay = isSelectedDay(day),
                showTitle = it < 7,
                isCurrentMonth = day.monthValue == valueMonth
            )
            val date = CalendarDate(
                year = day.year,
                month = day.monthValue,
                dayOfMonth = day.dayOfMonth,
                controller = controller,
            )
            dates.add(date)
        }
        val complexCalendarDate = ComplexCalendarDate(
            date = currentMonth,
            days = dates
        )
        if (addInit) {
            complexCalendarDates.add(0, complexCalendarDate)
            calendarPager.setCurrentItem(1, false)
        } else {
            complexCalendarDates.add(complexCalendarDate)
        }
    }

    private fun addListenerCalendarPager() {
        calendarPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            private fun addFirstValue(position: Int) {
                if (position == 0) {
                    val month = complexCalendarDates[position].date
                    addCalendarDates(month.minusMonths(1), true)
                }
            }

            private fun addLastValue(position: Int) {
                val totalItems = calendarPager.adapter?.itemCount ?: 0
                if (position == totalItems - 1) {
                    val month = complexCalendarDates[position].date
                    addCalendarDates(month.plusMonths(1))
                }
            }

            override fun onPageSelected(position: Int) {
                val monthNameToShow = complexCalendarDates[position].getMonthName()
                updateMonthName(monthNameToShow)
                if (isFirsTime.not()) {
                    addFirstValue(position)
                    addLastValue(position)
                }
            }
        })
    }

    override fun changeMonth(nameMonth: String) {
        updateMonthName(nameMonth)
    }

    override fun selectedDay(date: CalendarDate) {
        this.selectedDay = date
        updateComplexCalendar(date)
    }
}