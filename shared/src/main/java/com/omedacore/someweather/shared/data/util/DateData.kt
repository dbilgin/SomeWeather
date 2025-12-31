package com.omedacore.someweather.shared.data.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateData {
    // Format date (e.g., "Dec 30" or "Jan 1")
    fun getComplicationDate(applicationContext: Context): String {
        val calendar = Calendar.getInstance()
        val locale = applicationContext.resources.configuration.locales[0] ?: Locale.getDefault()
        val dateFormat = SimpleDateFormat("MMM d", locale)
        val dateText = dateFormat.format(calendar.time)
        return dateText
    }
}