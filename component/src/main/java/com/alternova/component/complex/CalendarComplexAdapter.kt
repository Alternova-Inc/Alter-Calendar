package com.alternova.component.complex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alternova.component.R
import com.alternova.component.complex.model.ComplexCalendarDate
import com.alternova.component.model.CalendarDate
import com.alternova.component.single.CalendarSingleAdapter

internal class CalendarComplexAdapter(
    private val calendarDates: MutableList<ComplexCalendarDate>,
    private val onAction: (CalendarDate) -> Unit
) : RecyclerView.Adapter<CalendarComplexAdapter.ViewHolder>() {
    class ViewHolder(view: View, onAction: (CalendarDate) -> Unit) : RecyclerView.ViewHolder(view) {
        private val calendarAdapter by lazy { CalendarSingleAdapter(mutableListOf(), onAction) }
        private val calendarRecycler by lazy { view.findViewById<RecyclerView>(R.id.calendarView) }
        fun bind(calendarDates: List<CalendarDate>) {
            calendarRecycler.layoutManager = GridLayoutManager(
                itemView.context, 7, GridLayoutManager.VERTICAL, false
            )
            calendarRecycler.adapter = calendarAdapter
            calendarAdapter.update(calendarDates.toMutableList())
        }
    }

    override fun getItemCount(): Int = calendarDates.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_complex_calendar,
            parent,
            false
        ).let { ViewHolder(it, onAction) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dates = calendarDates[position]
        holder.bind(dates.days)
    }

    fun update(calendarDates: List<ComplexCalendarDate>) =
        this.calendarDates.apply {
            clear()
            addAll(calendarDates)
        }.also { notifyDataSetChanged() }
}