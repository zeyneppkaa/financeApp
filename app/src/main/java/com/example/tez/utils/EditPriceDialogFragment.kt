package com.example.tez.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.tez.R

class EditPriceDialogFragment(
    private val initialPrice: String,
    private val onSave: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_price, null)
        val priceEditText = view.findViewById<EditText>(R.id.et_price)
        priceEditText.setText(initialPrice)

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()
    }
}