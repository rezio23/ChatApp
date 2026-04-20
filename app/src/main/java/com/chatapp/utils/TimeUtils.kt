package com.chatapp.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    fun formatMessageTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatChatListTime(timestamp: Long): String {
        val now = Calendar.getInstance()
        val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, msgCal) -> SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
            isYesterday(now, msgCal) -> "Yesterday"
            else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(today: Calendar, other: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, other)
    }
}
