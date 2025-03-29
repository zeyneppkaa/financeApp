package com.example.tez.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Bill(
    val name: String,
    val amount: Double,
    val month: String = getCurrentMonth(), // Ay bilgisi otomatik olarak alınacak
    val status: String, // "Paid" veya "Unpaid"
    val iconResId: Int // Drawable resource ID
) {
    constructor() : this("", 0.0, getCurrentMonth(), "Unpaid", 0)
    companion object {
        fun getCurrentMonth(): String {
            val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }
}