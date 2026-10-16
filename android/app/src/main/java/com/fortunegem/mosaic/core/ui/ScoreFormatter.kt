package com.fortunegem.mosaic.core.ui

object ScoreFormatter {

    fun points(value: Int): String {
        val digits = value.toString()
        if (digits.length <= 3) return digits
        val builder = StringBuilder()
        val head = digits.length % 3
        if (head > 0) {
            builder.append(digits.substring(0, head))
        }
        var cursor = head
        while (cursor < digits.length) {
            if (builder.isNotEmpty()) builder.append(' ')
            builder.append(digits.substring(cursor, cursor + 3))
            cursor += 3
        }
        return builder.toString()
    }

    fun clock(millis: Long): String {
        val safe = if (millis < 0L) 0L else millis
        val totalSeconds = (safe / 1000L).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val padded = if (seconds < 10) "0" + seconds else seconds.toString()
        return minutes.toString() + ":" + padded
    }
}
