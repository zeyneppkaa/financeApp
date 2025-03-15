package com.example.tez.model

import com.google.firebase.Timestamp

data class Incomes(
    val amount: Double = 0.0,  // Amount is a number (Firebase: Double)
    val category: String = "",  // Category is a string (Firebase: String)
    val date: Timestamp = Timestamp.now(),  // Date is a timestamp (Firebase: Timestamp)
    val name: String = ""  // Name is a string (Firebase: String)
) {
    // Parametresiz yapıcı metod Firebase için gereklidir
    constructor() : this(0.0, "", Timestamp.now(), "")
}
