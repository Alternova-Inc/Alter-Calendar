package com.alternova.component.complex

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.viewpager2.widget.ViewPager2
import com.alternova.component.R
import com.alternova.component.common.BaseCalendarComponent
import com.alternova.component.common.getDayOfWeek
import com.alternova.component.complex.model.ComplexCalendarDate
import com.alternova.component.model.CalendarController
import com.alternova.component.model.CalendarDate
import java.time.LocalDate

private const val COUNT_MONTH = 42

internal class ComplexCalendarComponent(
    context: Context,
    attrs: AttributeSet
) : BaseCalendarComponent(context, attrs) {

    private val complexDates: MutableList<ComplexCalendarDate> = mutableListOf()
    private var isScrolledAvailable: Boolean = false

    private val calendarComplexAdapter by lazy {
        CalendarComplexAdapter(
            mutableListOf(
                ComplexCalendarDate(
                    LocalDate.now(),
                    mutableListOf(CalendarDate(2000, 2, 2, CalendarController(true)))
                )
            ), onItemClick
        )
    }
    private val calendarPager by lazy { findViewById<ViewPager2>(R.id.calendar) }
    private val currentDate: LocalDate by lazy { LocalDate.now() }

    init {
        initView()
        initPager()
        initData()
        addListener()
    }

    override fun initView() {
        LayoutInflater.from(context).inflate(R.layout.component_complex_calendar, this, true)
    }

    private fun initPager() {
        calendarPager.adapter = calendarComplexAdapter
    }

    override fun initData() {
        buildData(currentDate)
        buildData(currentDate.minusMonths(1))
        notifyAdapter { calendarPager.setCurrentItem(1, false) }
    }

    fun setScrollAvailability(isScrolledAvailable: Boolean) {
        this.isScrolledAvailable = isScrolledAvailable
        calendarComplexAdapter.changeViewByWeek(isScrolledAvailable)
    }

    private fun buildData(pivotDate: LocalDate) {
        initDate?.apply { if (pivotDate.monthValue < this.monthValue) return }
        val dates = mutableListOf<CalendarDate>()
        val firstDayOfMonth = pivotDate.withDayOfMonth(1)
        val starCalendar = firstDayOfMonth.minusDays(
            firstDayOfMonth.dayOfWeek.getDayOfWeek().toLong()
        )
        val pivotMonth = pivotDate.month.value
        repeat(COUNT_MONTH) { index ->
            val date = starCalendar.plusDays(index.toLong())
            val isBeforeCurrentDate = date.isAfter(currentDate).not()
            val isAfterInitDate = initDate?.let { date.isAfter(it) } ?: true
            val controller = CalendarController(
                isSelectedDay = date.isEqual(currentDate),
                showTitle = index < 7,
                isCurrentMonth = date.monthValue == pivotMonth,
                dayEnabled = isBeforeCurrentDate && isAfterInitDate,
            )
            val calendarDate = CalendarDate(
                year = date.year,
                month = date.monthValue,
                dayOfMonth = date.dayOfMonth,
                controller = controller,
            )
            dates.add(calendarDate)
        }
        if (isScrolledAvailable) {
            var flagSelectedDay = 0
            dates.forEachIndexed { index, calendarDate ->
                if (calendarDate.controller.isSelectedDay) {
                    flagSelectedDay = index - calendarDate.getLocalDate().dayOfWeek.getDayOfWeek()
                }
            }

            for (index in flagSelectedDay..(flagSelectedDay + 6)) {
                dates[index].controller.isDaySelectedInViewWeek = true
            }
        }
        val complexCalendarDate = ComplexCalendarDate(
            date = pivotDate,
            days = dates
        )
        complexDates.add(0, complexCalendarDate)
    }

    override fun addListener() {
        calendarPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            private fun addBeforeValue(position: Int) {
                initDate?.apply {
                    val month = complexDates[position].date
                    val beforeMonth = month.minusMonths(1)
                    buildData(beforeMonth)
                    notifyAdapter {
                        if (beforeMonth.monthValue >= this.monthValue) {
                            calendarPager.setCurrentItem(1, false)
                        }
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val monthName = complexDates[position].getMonthName()
                controllerListener?.changeMonth(monthName)
                if (position == 0) addBeforeValue(position)
            }
        })
    }

    override fun updateSelectedDay(selectedDay: CalendarDate) {
        complexDates.forEach { calendar ->
            calendar.days.forEach { day ->
                day.controller.isSelectedDay = false
                day.controller.isDaySelectedInViewWeek = false
            }
            calendar.days.find {
                it.isEqualsToOtherDate(selectedDay.year, selectedDay.month, selectedDay.dayOfMonth)
            }?.controller?.isSelectedDay = true
            if (isScrolledAvailable) {
                var flagSelectedDay = -1
                calendar.days.forEachIndexed { index, calendarDate ->
                    if (calendarDate.controller.isSelectedDay) {
                        flagSelectedDay =
                            index - calendarDate.getLocalDate().dayOfWeek.getDayOfWeek()
                    }
                }
                if (flagSelectedDay >= 0) {
                    for (index in flagSelectedDay..(flagSelectedDay + 6)) {
                        calendar.days[index].controller.isDaySelectedInViewWeek = true
                    }
                }
            }
        }
        calendarComplexAdapter.update(complexDates)
    }

    override fun notifyAdapter(onMoveToPosition: () -> Unit) {
        calendarPager.post {
            calendarComplexAdapter.update(complexDates)
            onMoveToPosition()
        }
    }

    override fun setInitCalendarDate(initDate: LocalDate?) {
        this.initDate = initDate.also { complexDates.clear() }
        initData()
        this.initDate?.apply {
            complexDates.forEach { calendar ->
                calendar.days.forEach { day ->
                    if (day.getLocalDate().isBefore(this)) {
                        day.controller.dayEnabled = false
                    }
                }
            }
        }
    }
}