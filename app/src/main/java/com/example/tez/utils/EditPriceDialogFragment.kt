package com.example.tez.utils

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.tez.R

class EditPriceDialogFragment(
    private val initialPrice: String,
    private val categoryName: String,
    private val onPriceConfirmed: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_price, null)

        val priceEditText = view.findViewById<EditText>(R.id.et_price)
        val categoryTextView = view.findViewById<TextView>(R.id.categoryName)
        val saveButton = view.findViewById<ImageButton>(R.id.imgBtnEditPrice)

        // Başlangıç değerlerini ata
        priceEditText.setText(initialPrice)
        categoryTextView.text = categoryName

        // Kayıt butonuna tıklanınca:
        saveButton.setOnClickListener {
            val newPrice = priceEditText.text.toString()
            onPriceConfirmed(newPrice)
            dismiss() // Dialog'u kapat
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)
        dialog.setCancelable(true)
        return dialog
    }
}
