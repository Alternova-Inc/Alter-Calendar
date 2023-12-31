package com.alternova.component.single

import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alternova.component.R
import com.alternova.component.model.CalendarDate

class CalendarSingleAdapter(
    private val array: MutableList<CalendarDate>,
    private val onAction: (CalendarDate) -> Unit
) : RecyclerView.Adapter<CalendarSingleAdapter.ViewHolder>() {
    class ViewHolder(view: View, private val onAction: (CalendarDate) -> Unit) :
        RecyclerView.ViewHolder(view) {
        private val containerDay: LinearLayout = view.findViewById(R.id.containerDay)
        private val nameDay: TextView = view.findViewById(R.id.nameDay)
        private val backgroundDay: View = view.findViewById(R.id.backgroundDay)
        private val numberDay: TextView = view.findViewById(R.id.numberDay)

        fun bind(calendarDate: CalendarDate) {
            if (calendarDate.controller.dayEnabled) {
                containerDay.setOnClickListener { onAction.invoke(calendarDate) }
            }
            buildTitle(calendarDate)
            buildNumber(calendarDate)
            changeColor(calendarDate)
        }

        private fun buildTitle(calendarDate: CalendarDate) {
            if (calendarDate.controller.showTitle) {
                nameDay.visibility = View.VISIBLE
                nameDay.text = calendarDate.getNameDayOfWeek()
            } else {
                nameDay.visibility = View.GONE
            }
        }

        private fun buildNumber(calendarDate: CalendarDate) {
            numberDay.text = calendarDate.dayOfMonth.toString()
        }

        private fun changeColor(calendarDate: CalendarDate) {
            if (calendarDate.controller.isSelectedDay) {
                nameDay.setTypeface(null, BOLD)
                backgroundDay.isVisible = true
                numberDay.setTextColor(Color.WHITE)
                return
            }
            nameDay.setTypeface(null, NORMAL)
            backgroundDay.isVisible = false
            val colorNumber = if (calendarDate.controller.isCurrentMonth) Color.BLACK
            else Color.GRAY
            numberDay.setTextColor(colorNumber)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = LayoutInflater
        .from(parent.context).inflate(R.layout.item_day_calendar, parent, false)
        .let { ViewHolder(it, onAction) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = array[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int = array.size

    fun update(calendarDates: MutableList<CalendarDate>) = array.apply {
        clear()
        addAll(calendarDates)
    }.also { notifyDataSetChanged() }
}