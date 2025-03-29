package com.example.tez.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.databinding.ItemBillBinding
import com.example.tez.model.Bill

class BillAdapter(
    private val bills: List<Bill>,
    private val onEditClick: (Bill) -> Unit,
    private val onConfirmClick: (Bill) -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    inner class BillViewHolder(val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = bills[position]
        with(holder.binding) {
            ivBillIcon.setImageResource(bill.iconResId)
            tvBillName.text = bill.name
            tvBillStatus.text = bill.status
            tvBillAmount.text = "${'$'}${bill.amount}"

            btnEditBill.setOnClickListener { onEditClick(bill) }
            btnConfirmBill.setOnClickListener { onConfirmClick(bill) }
        }
    }

    override fun getItemCount(): Int = bills.size
}