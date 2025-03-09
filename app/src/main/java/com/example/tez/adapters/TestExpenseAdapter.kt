package com.example.tez.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.R
import com.example.tez.databinding.ItemExpenseBinding
import com.example.tez.model.Expense
import java.text.SimpleDateFormat
import java.util.Locale

class TestExpenseAdapter(private val expenseList: List<Expense>) :
    RecyclerView.Adapter<TestExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.binding.apply {
            tvExpenseName.text = expense.name
            tvExpenseCategory.text = expense.category
            tvExpenseAmount.text = "- ${expense.amount} ₺"
            // Date'ı Timestamp'ten Date'e çevirip formatla
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(expense.date.toDate()) // toDate() kullanarak Timestamp'i Date'e çeviriyoruz.
            tvExpenseDate.text = formattedDate

            ivExpenseIcon.setImageResource(getCategoryIcon(expense.category))
        }
    }

    override fun getItemCount() = expenseList.size

    private fun getCategoryIcon(category: String): Int {
        return when (category) {
            "Restaurant" -> R.drawable.ic_restaurant
            "Transport" -> R.drawable.ic_transportation
            "Health" -> R.drawable.ic_health
            else -> R.drawable.ic_add
        }
    }
}

