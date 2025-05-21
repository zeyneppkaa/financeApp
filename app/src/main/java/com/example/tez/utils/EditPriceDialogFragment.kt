package com.example.tez.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.tez.R
import com.example.tez.model.Expense
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class EditPriceDialogFragment(
    private val initialPrice: String,
    private val categoryName: String,
    private val userId: String // Firestore için kullanıcı id
) : DialogFragment() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_price, null)
        val priceEditText = view.findViewById<EditText>(R.id.et_price)
        val categoryTextView = view.findViewById<TextView>(R.id.categoryName)
        val nameEditText = view.findViewById<EditText>(R.id.et_expense_name)
        val imgBtnEditPrice = view.findViewById<ImageButton>(R.id.imgBtnEditPrice)

        priceEditText.setText(initialPrice)
        categoryTextView.text = categoryName

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()

        imgBtnEditPrice.setOnClickListener {
            val priceText = priceEditText.text.toString()
            val amount = priceText.toDoubleOrNull()
            val expenseName = nameEditText.text.toString().trim()

            if (amount == null) {
                Toast.makeText(requireContext(), "Lütfen geçerli bir fiyat girin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (expenseName.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen gider adı girin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (userId.isEmpty()) {
                Toast.makeText(requireContext(), "Kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                amount = amount,
                category = categoryName,
                name = expenseName,
                date = Timestamp.now()
            )

            firestore.collection("users")
                .document(userId)
                .collection("expenses")
                .add(expense)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Gider başarıyla eklendi.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return dialog
    }
}
