package com.alternova.altercalendar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alternova.component.CalendarListener
import com.alternova.component.CalendarView
import com.alternova.component.model.CalendarDate

class MainActivity : AppCompatActivity(), CalendarListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val calendarView: CalendarView = findViewById(R.id.calendar)
        calendarView.addOnCalendarListener(this)
    }

    override fun onSelectedDate(date: CalendarDate) {
        Toast.makeText(this, date.toString(), Toast.LENGTH_SHORT).show()
    }
}