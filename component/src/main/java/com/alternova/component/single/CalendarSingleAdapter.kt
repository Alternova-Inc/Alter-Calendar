package com.alternova.component.single

import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.alternova.component.R
import com.alternova.component.model.CalendarDate

class CalendarSingleAdapter(
    private val array: MutableList<CalendarDate>,
    private val onAction: (CalendarDate) -> Unit
) : RecyclerView.Adapter<CalendarSingleAdapter.ViewHolder>() {

    private var isViewByWeek: Boolean = true

    fun changeViewByWeek(isViewByWeek: Boolean) {
        this.isViewByWeek = isViewByWeek
        notifyDataSetChanged()
    }

    class ViewHolder(view: View, private val onAction: (CalendarDate) -> Unit) :
        RecyclerView.ViewHolder(view) {
        private val containerDay: ConstraintLayout = view.findViewById(R.id.containerDay)
        private val nameDay: TextView = view.findViewById(R.id.nameDay)
        private val backgroundDay: View = view.findViewById(R.id.backgroundDay)
        private val backgroundWeek: View = view.findViewById(R.id.backgroundWeek)
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
            when {
                calendarDate.controller.isSelectedDay -> {
                    nameDay.setTypeface(null, BOLD)
                    backgroundWeek.isVisible = false
                    backgroundDay.isVisible = true
                    numberDay.setTextColor(Color.WHITE)
                    return
                }

                else -> {
                    nameDay.setTypeface(null, NORMAL)
                    backgroundWeek.isVisible = false
                    backgroundDay.isVisible = false
                    var colorNumber = if (calendarDate.controller.isCurrentMonth) Color.BLACK
                    else Color.GRAY
                    colorNumber = if (calendarDate.controller.dayEnabled) colorNumber
                    else Color.GRAY
                    numberDay.setTextColor(colorNumber)
                }
            }
        }

        fun borderEnd(isViewByWeek: Boolean) {
            if (isViewByWeek) {
                numberDay.setTextColor(Color.BLACK)
                backgroundWeek.isVisible = true
                backgroundDay.isVisible = false
                backgroundWeek.setBackgroundResource(R.drawable.right_background)
            } else {
                backgroundInvisible()
            }
        }

        fun borderStart(isViewByWeek: Boolean) {
            if (isViewByWeek) {
                numberDay.setTextColor(Color.BLACK)
                backgroundWeek.isVisible = true
                backgroundDay.isVisible = false
                backgroundWeek.setBackgroundResource(R.drawable.left_background)
            } else {
                backgroundInvisible()
            }
        }

        fun borderDefault(isViewByWeek: Boolean) {
            if (isViewByWeek) {
                numberDay.setTextColor(Color.BLACK)
                backgroundWeek.isVisible = true
                backgroundDay.isVisible = false
                backgroundWeek.setBackgroundResource(R.drawable.default_background)
            } else {
                backgroundInvisible()
            }
        }

        fun backgroundInvisible() {
            numberDay.setTextColor(Color.BLACK)
            backgroundWeek.isVisible = true
            backgroundDay.isVisible = false
            backgroundWeek.setBackgroundColor(Color.TRANSPARENT)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater
            .from(parent.context).inflate(R.layout.item_day_calendar, parent, false)
            .let { ViewHolder(it, onAction) }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = array[position]
        holder.bind(day)
        if (array.size == 7 && isViewByWeek) {
            when (position) {
                0 -> holder.borderStart(isViewByWeek)
                1 -> holder.borderDefault(isViewByWeek)
                2 -> holder.borderDefault(isViewByWeek)
                3 -> holder.borderDefault(isViewByWeek)
                4 -> holder.borderDefault(isViewByWeek)
                5 -> holder.borderDefault(isViewByWeek)
                6 -> holder.borderEnd(isViewByWeek)
                else -> holder.backgroundInvisible()
            }
        }
    }

    override fun getItemCount(): Int = array.size

    fun update(calendarDates: MutableList<CalendarDate>) = array.apply {
        clear()
        addAll(calendarDates)
    }.also { notifyDataSetChanged() }
}