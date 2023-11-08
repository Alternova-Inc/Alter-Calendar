package com.alternova.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.alternova.component.complex.CalendarComplexAdapter
import com.alternova.component.complex.model.ComplexCalendarDate
import com.alternova.component.model.CalendarDate
import com.alternova.component.single.CalendarSingleAdapter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.format.TextStyle
import java.util.Locale

class CalendarView(private val context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    private var selectedDay: CalendarDate? = null
    private var calendarListener: CalendarListener? = null
    private val singleCalendarDates = mutableListOf<CalendarDate>()
    private val onSingleAction: (CalendarDate) -> Unit = { date ->
        updateSingleCalendar(date)
        updateMonthName(date.getLocalDate())
        updateComplexCalendar(date)
        calendarListener?.onSelectedDate(date)
        selectedDay = date
    }
    private val calendarAdapter by lazy { CalendarSingleAdapter(mutableListOf(), onSingleAction) }
    private val calendarRecycler by lazy { findViewById<RecyclerView>(R.id.bodySingleCalendar) }
    private val titleCalendar: TextView by lazy { findViewById(R.id.titleCalendar) }
    private val currentDate = startInLastSunday()
    private var monthNameToShow = getMonthNameToShow()
    private var currentArrowIcon: Int = R.drawable.ic_arrow_down
    private var isFirsTime = true

    private val complexCalendarDates: MutableList<ComplexCalendarDate> = mutableListOf()
    private val onComplexAction: (CalendarDate) -> Unit = {
        updateComplexCalendar(it)
        updateMonthName(it.getLocalDate())
        updateSingleCalendar(it)
        calendarListener?.onSelectedDate(it)
        selectedDay = it
    }
    private val calendarComplexAdapter by lazy {
        CalendarComplexAdapter(mutableListOf(), onComplexAction)
    }
    private val calendarPager by lazy { findViewById<ViewPager2>(R.id.bodyComplexCalendar) }

    init {
        LayoutInflater.from(context).inflate(R.layout.calendar_view, this, true)
        initView()
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

    private fun updateSingleCalendar(date: CalendarDate){
        singleCalendarDates.forEach { it.isSelectedDay = false }
        singleCalendarDates.find {
            it.isEqualsToOtherDate(date.year, date.month, date.dayOfMonth)
        }?.isSelectedDay = true
        calendarAdapter.update(singleCalendarDates)
    }

    private fun updateComplexCalendar(date: CalendarDate){
        complexCalendarDates.forEach { parent ->
            parent.days.forEach { child -> child.isSelectedDay = false }
            parent.days.find { child ->
                child.isEqualsToOtherDate(date.year, date.month, date.dayOfMonth)
            }?.isSelectedDay = true
        }
        calendarComplexAdapter.update(complexCalendarDates)
    }

    private fun initView() {
        calendarRecycler.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        calendarRecycler.adapter = calendarAdapter
        initSingleCalendar()
        initComplexCalendar()
        initTitleCalendar()
    }

    private fun getMonthNameToShow(pivotDate: LocalDate = currentDate) =
        pivotDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    private fun isSelectedDay(pivotDate: LocalDate): Boolean = LocalDate.now().let {
        pivotDate.dayOfMonth == it.dayOfMonth &&
                pivotDate.monthValue == it.monthValue &&
                pivotDate.year == it.year
    }

    private fun addRepeat(differ: Int, pivotDate: LocalDate) {
        repeat(differ) {
            val day = pivotDate.plusDays(it.toLong() + 1)
            val date = CalendarDate(
                year = day.year,
                month = day.monthValue,
                dayOfMonth = day.dayOfMonth,
                isSelectedDay = isSelectedDay(day)
            )
            singleCalendarDates.add(date)
        }
    }

    private fun initSingleCalendar() {
        val startDate = currentDate.minusDays(10)
        addRepeat(20, startDate)
        calendarRecycler.post {
            calendarAdapter.update(singleCalendarDates)
            val pivot = singleCalendarDates.indexOfFirst { it.dayOfMonth == currentDate.dayOfMonth }
            calendarRecycler.scrollToPosition(pivot)
        }
        addListenerCalendarRecycler()
    }

    private fun addListenerCalendarRecycler() {
        calendarRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollHorizontally(1)) {
                    addEndDays()
                }
                if (!recyclerView.canScrollHorizontally(-1)) {
                    addStartDays()
                }
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val pivot: Int = (firstVisibleItemPosition + lastVisibleItemPosition) / 2
                val pivotDate = singleCalendarDates[pivot].getLocalDate()
                updateMonthName(pivotDate)
            }
        })
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

    private fun addStartDays() {
        val firstDate = singleCalendarDates.firstOrNull()?.getLocalDate() ?: return
        repeat(10) {
            val day = firstDate.minusDays(it.toLong() + 1)
            val date = CalendarDate(
                year = day.year,
                month = day.monthValue,
                dayOfMonth = day.dayOfMonth,
                isSelectedDay = isSelectedDay(day)
            )
            singleCalendarDates.add(0, date)
        }
        calendarRecycler.post {
            calendarAdapter.update(singleCalendarDates)
            calendarRecycler.scrollToPosition(18)
        }
    }

    private fun addEndDays() {
        val lastDate = singleCalendarDates.lastOrNull()?.getLocalDate() ?: return
        val endDate = lastDate.plusDays(10)
        val differ = Period.between(lastDate, endDate).days
        addRepeat(differ, lastDate)
        calendarRecycler.post {
            calendarAdapter.update(singleCalendarDates)
        }
    }

    fun addOnCalendarListener(calendarListener: CalendarListener) {
        this.calendarListener = calendarListener
    }

    private fun initComplexCalendar() {
        calendarRecycler.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        calendarPager.adapter = calendarComplexAdapter
        addCalendarDates()
        calendarPager.setCurrentItem(1, false)
        addListenerCalendarPager()
    }

    private fun addCalendarDates(startMonth: LocalDate = currentDate, addInit: Boolean = false) {
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
        val firstOfWeekMonth = firstDayMonth.minusDays(
            getDayOfWeek(firstDayMonth.dayOfWeek).toLong()
        )
        val valueMonth = currentMonth.month.value
        repeat(42) {
            val day = firstOfWeekMonth.plusDays(it.toLong())
            val date = CalendarDate(
                year = day.year,
                month = day.monthValue,
                dayOfMonth = day.dayOfMonth,
                isSelectedDay = isSelectedDay(day),
                showTitle = it < 7,
                isCurrentMonth = day.monthValue == valueMonth
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
}