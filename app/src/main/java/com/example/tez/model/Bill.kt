package com.example.tez.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Bill(
    val id: String = "", // Firestore'dan gelen belge ID'si
    val name: String,
    var amount: Double,
    val month: String = getCurrentMonth(),
    var status: String,
    val iconResId: Int
) {
    constructor() : this("", "", 0.0, getCurrentMonth(), "Unpaid", 0)

    companion object {
        fun getCurrentMonth(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }
}
