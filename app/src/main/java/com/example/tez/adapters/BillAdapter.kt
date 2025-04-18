package com.example.tez.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.R
import com.example.tez.databinding.ItemBillBinding
import com.example.tez.model.Bill

class BillAdapter(
    private val bills: MutableList<Bill>,
    private val onEditClick: (Bill) -> Unit,
    private val onConfirmClick: (Bill) -> Unit,
    private val onAmountChanged: (Bill) -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    inner class BillViewHolder(val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = bills[position]
        applyStatusUI(bill, holder.binding)

        with(holder.binding) {
            ivBillIcon.setImageResource(bill.iconResId)
            tvBillName.text = bill.name
            tvBillStatus.text = bill.status
            tvBillAmount.text = "${bill.amount} ${'₺'}"
            etBillAmount.setText(bill.amount.toString())

            btnEditBill.setOnClickListener {
                tvBillAmount.visibility = View.GONE
                etBillAmount.visibility = View.VISIBLE
                btnEditBill.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.bill_name_text_default), PorterDuff.Mode.SRC_IN)
                etBillAmount.requestFocus()
            }

            // Kullanıcı Enter tuşuna basınca değişiklik kaydedilsin
            etBillAmount.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    saveAmountChange(bill, etBillAmount, tvBillAmount)
                    true
                } else {
                    false
                }
            }

            // Kullanıcı EditText'ten çıktığında değişiklik kaydedilsin
            etBillAmount.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    saveAmountChange(bill, etBillAmount, tvBillAmount)
                    btnEditBill.setColorFilter(null)

                }
            }

            btnConfirmBill.setOnClickListener {
                bill.status = "Paid"
                onConfirmClick(bill)
                applyStatusUI(bill, this)
            }
        }
    }

    // Amount değişikliğini kaydetme fonksiyonu
    private fun saveAmountChange(bill: Bill, etBillAmount: EditText, tvBillAmount: TextView) {
        val newAmount = etBillAmount.text.toString().toDoubleOrNull()
        if (newAmount != null && newAmount != bill.amount) {
            val updatedBill = bill.copy(amount = newAmount) // Yeni nesne oluşturuyoruz

            tvBillAmount.text = "${newAmount} ₺"
            onAmountChanged(updatedBill) // Güncellenmiş nesneyi geri yolluyoruz

            tvBillAmount.visibility = View.VISIBLE
            etBillAmount.visibility = View.GONE
        } else {
            // Değişiklik yapılmamışsa, sadece görünüm değiştir
            tvBillAmount.visibility = View.VISIBLE
            etBillAmount.visibility = View.GONE
        }
    }

    private fun applyStatusUI(bill: Bill, binding: ItemBillBinding) {
        if (bill.status == "Paid") {
            binding.tvBillStatus.text = "Paid"
            binding.itemBillLinear.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.paid_bill_item_bg))
            binding.tvBillName.setTextColor(Color.WHITE)
            binding.ivBillIcon.setColorFilter(Color.WHITE)
            binding.btnEditBill.visibility = View.GONE
            binding.btnConfirmBill.setImageResource(R.drawable.ic_check_green)
            binding.btnConfirmBill.isEnabled = false
        } else {
            binding.tvBillStatus.text = "Unpaid"
            binding.itemBillLinear.setBackgroundColor(Color.WHITE)
            binding.btnEditBill.visibility = View.VISIBLE
            binding.tvBillName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.btn_edit_bill_active))
            binding.ivBillIcon.setColorFilter(null)
            binding.btnConfirmBill.setImageResource(R.drawable.ic_check_default)
            binding.btnConfirmBill.isEnabled = true
        }
    }

    override fun getItemCount(): Int = bills.size
}