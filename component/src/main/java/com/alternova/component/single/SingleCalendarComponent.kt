package com.alternova.component.single

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alternova.component.R
import com.alternova.component.common.BaseCalendarComponent
import com.alternova.component.model.CalendarController
import com.alternova.component.model.CalendarDate
import java.time.LocalDate

private const val START_WEEK_PIVOT = 9

internal class SingleCalendarComponent(
    context: Context,
    attrs: AttributeSet
) : BaseCalendarComponent(context, attrs) {
    private val dates = mutableListOf<CalendarDate>()
    private val calendarAdapter by lazy {
        CalendarSingleAdapter(mutableListOf(), onItemClick)
    }
    private val calendarRecycler by lazy { findViewById<RecyclerView>(R.id.dates) }
    private val currentDate by lazy { LocalDate.now() }

    private var isScrolledAvailable: Boolean = true

    init {
        initView()
        initRecycler()
        initData()
        addListener()
    }

    override fun initView() {
        LayoutInflater.from(context).inflate(R.layout.component_single_calendar, this, true)
    }

    override fun initData() {
        val plusPivot = if (isScrolledAvailable) 0 else 2
        val dayOfWeek = currentDate.dayOfWeek.getDayOfWeek() + plusPivot
        val startCalendar = currentDate.minusDays(dayOfWeek.toLong())
        val startWeekPivot = if (isScrolledAvailable) 7 else 9
        repeat(startWeekPivot) {
            val date = startCalendar.plusDays(it.toLong())
            val controller = CalendarController(
                isSelectedDay = date.isEqual(currentDate),
                dayEnabled = date.isAfter(currentDate).not(),
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

    private fun initRecycler() {
        calendarRecycler.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        calendarRecycler.adapter = calendarAdapter
    }

    override fun addListener() {
        calendarRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                notifyParentChangeMonthName(recyclerView)
                if (isScrolledAvailable) {
                    return
                }
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
            repeat(START_WEEK_PIVOT) { index ->
                val date = firstDate.minusDays(index.toLong() + 1)
                if (date.isBefore(initDate)) return@repeatBlock
                val isBeforeCurrentDate = date.isAfter(currentDate).not()
                val isAfterInitDate = initDate?.let { date.isAfter(it) } ?: true
                val controller = CalendarController(
                    isSelectedDay = date.isEqual(currentDate),
                    dayEnabled = isBeforeCurrentDate && isAfterInitDate
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
        notifyAdapter(onMoveToPosition = {
            calendarRecycler.scrollToPosition(pivot)
        })
    }

    override fun notifyAdapter(onMoveToPosition: () -> Unit) {
        calendarRecycler.post {
            calendarAdapter.update(dates)
            onMoveToPosition.invoke()
        }
    }

    override fun updateSelectedDay(selectedDay: CalendarDate) {
        dates.forEach { date -> date.controller.isSelectedDay = false }
        dates.find {
            it.isEqualsToOtherDate(selectedDay.year, selectedDay.month, selectedDay.dayOfMonth)
        }?.controller?.isSelectedDay = true
        calendarAdapter.update(dates)
    }

    override fun setInitCalendarDate(initDate: LocalDate?) {
        this.initDate = initDate?.apply {
            dates.forEach { date ->
                if (date.getLocalDate().isBefore(this)) {
                    date.controller.dayEnabled = false
                }
            }
        }
    }

    fun setScrollAvailability(isScrolledAvailable: Boolean) {
        this.isScrolledAvailable = isScrolledAvailable
        calendarAdapter.changeViewByWeek(isScrolledAvailable)
    }
}