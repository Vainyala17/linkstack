package com.hp77.linkstash.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(date)
    }

    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val today = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply { time = date }

        return when {
            isSameDay(today, dateCalendar) -> "Today"
            isYesterday(today, dateCalendar) -> "Yesterday"
            isThisYear(today, dateCalendar) -> SimpleDateFormat("MMMM d", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(today: Calendar, date: Calendar): Boolean {
        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, date)
    }

    private fun isThisYear(today: Calendar, date: Calendar): Boolean {
        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR)
    }
}
