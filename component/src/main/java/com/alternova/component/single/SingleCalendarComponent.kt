package com.alternova.component.single

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alternova.component.CalendarListener
import com.alternova.component.R
import com.alternova.component.model.CalendarController
import com.alternova.component.model.CalendarDate
import java.time.DayOfWeek
import java.time.LocalDate

private const val START_WEEK_PIVOT = 9L

internal class SingleCalendarComponent(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {
    private var calendarListener: CalendarListener? = null
    private var controllerListener: SingleControllerListener? = null
    private var initDate: LocalDate? = null

    private val onItemClick: (CalendarDate) -> Unit = {
        updateSelectedDay(it)
        calendarListener?.onSelectedDate(it)
        controllerListener?.selectedDay(it)
    }
    private val dates = mutableListOf<CalendarDate>()
    private val calendarAdapter by lazy {
        CalendarSingleAdapter(mutableListOf(), onItemClick)
    }
    private val calendarRecycler by lazy { findViewById<RecyclerView>(R.id.dates) }
    private val currentDate = LocalDate.now()

    init {
        initView()
        initRecycler()
        initData()
        addListener()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.component_single_calendar, this, true)
    }

    private fun initData() {
        val dayOfWeek = currentDate.dayOfWeek.getDayOfWeek() + 2
        val startCalendar = currentDate.minusDays(dayOfWeek.toLong())
        repeat(START_WEEK_PIVOT.toInt()) {
            val date = startCalendar.plusDays(it.toLong())
            val controller = CalendarController(
                isSelectedDay = date.isEqual(currentDate),
                dayEnabled = date.isBefore(currentDate),
            )
            val calendar = CalendarDate(
                date.year,
                date.monthValue,
                date.dayOfMonth,
                controller = controller
            )
            dates.add(calendar)
        }
        notifyAdapter {
            calendarRecycler.scrollToPosition(2)
        }
    }

    private fun DayOfWeek.getDayOfWeek(): Int = when (this) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }

    private fun initRecycler() {
        calendarRecycler.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        calendarRecycler.adapter = calendarAdapter
    }

    private fun addListener() {
        calendarRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                notifyParentChangeMonthName(recyclerView)
                val directionLeft = -1
                if (recyclerView.canScrollHorizontally(directionLeft).not()) {
                    addStartDays()
                }
            }
        })
    }

    private fun notifyParentChangeMonthName(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val pivot: Int = (firstVisibleItemPosition + lastVisibleItemPosition) / 2
        val nameMonth = dates[pivot].getNameMont()
        controllerListener?.changeMonth(nameMonth)
    }

    private fun addStartDays() {
        val firstDate = dates.firstOrNull()?.getLocalDate() ?: return
        var pivot = 6
        run repeatBlock@{
            repeat(START_WEEK_PIVOT.toInt()) {
                val date = firstDate.minusDays(it.toLong() + 1)
                if (date.isBefore(initDate)) return@repeatBlock
                val controller = CalendarController(
                    isSelectedDay = date.isEqual(currentDate),
                )
                val calendar = CalendarDate(
                    date.year,
                    date.monthValue,
                    date.dayOfMonth,
                    controller = controller
                )
                dates.add(0, calendar)
                pivot++
            }
        }
        notifyAdapter(onScrollToPosition = {
            calendarRecycler.scrollToPosition(pivot)
        })
    }

    private fun notifyAdapter(onScrollToPosition: () -> Unit) {
        calendarRecycler.post {
            calendarAdapter.update(dates)
            onScrollToPosition.invoke()
        }
    }

    private fun updateSelectedDay(selectedDay: CalendarDate) {
        dates.forEach { date -> date.controller.isSelectedDay = false }
        dates.find {
            it.isEqualsToOtherDate(selectedDay.year, selectedDay.month, selectedDay.dayOfMonth)
        }?.controller?.isSelectedDay = true
        calendarAdapter.update(dates)
    }

    fun addOnCalendarListener(calendarListener: CalendarListener) {
        this.calendarListener = calendarListener
    }

    fun addOnControllerListener(controllerListener: SingleControllerListener) {
        this.controllerListener = controllerListener
    }

    fun setInitCalendarDate(initDate: LocalDate?) {
        this.initDate = initDate?.apply {
            dates.forEach { date ->
                if (date.getLocalDate().isBefore(this)) {
                    date.controller.dayEnabled = false
                }
            }
        }
    }
}