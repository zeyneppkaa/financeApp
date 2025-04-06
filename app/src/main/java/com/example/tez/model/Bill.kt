package com.example.tez.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Bill(
    val id: String = "", // Firestore'dan gelen belge ID'si
    val name: String,
    var amount: Double,
    val month: String = getCurrentMonth(), // Ay bilgisi otomatik olarak alınacak
    var status: String, // "Paid" veya "Unpaid"
    val iconResId: Int // Drawable resource ID
) {
    constructor() : this("", "", 0.0, getCurrentMonth(), "Unpaid", 0)

    companion object {
        fun getCurrentMonth(): String {
            val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }
}
