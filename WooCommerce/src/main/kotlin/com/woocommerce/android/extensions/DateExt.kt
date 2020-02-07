package com.woocommerce.android.extensions

import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.formatToYYYYmm(): String = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(this)

fun Date.formatToYYYY(): String = SimpleDateFormat("yyyy", Locale.getDefault()).format(this)

fun Date.formatToYYYYWmm(): String = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()).format(this)

fun Date.formatToMMMMdd(): String = SimpleDateFormat("MMMM dd", Locale.getDefault()).format(this)

fun Date.formatToDD(): String = SimpleDateFormat("d", Locale.getDefault()).format(this)

fun Date.formatToMMMdd(): String = SimpleDateFormat("MMM d", Locale.getDefault()).format(this)

fun Date.formatToMMMddYYYY(): String = SimpleDateFormat("MMM d, YYYY", Locale.getDefault()).format(this)

/**
 * Returns a date with the passed GMT offset applied - note that this assumes the current date is GMT
 */
fun Date.gmtDateWithOffset(gmtOffset: Int): Date {
    val secondsOffset = 3600 * gmtOffset // 3600 is the number of seconds in an hour
    return Date(this.time + secondsOffset)
}

fun Date.formatToEEEEMMMddhha(): String {
    val symbols = DateFormatSymbols(Locale.getDefault())
    symbols.amPmStrings = arrayOf("am", "pm")
    val dateFormat = SimpleDateFormat("EEEE, MMM dd › ha", Locale.getDefault())
    dateFormat.dateFormatSymbols = symbols
    return dateFormat.format(this)
}
