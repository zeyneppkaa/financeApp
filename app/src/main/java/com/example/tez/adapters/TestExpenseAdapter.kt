package com.example.tez

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.databinding.ItemExpenseBinding
import com.example.tez.model.Expense

class TestExpenseAdapter(private val expenses: List<Expense>) :
    RecyclerView.Adapter<TestExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: Expense) {
            binding.tvExpenseCategory.text = expense.category
            binding.tvExpenseName.text = expense.name
            binding.tvExpenseAmount.text = "${expense.price} USD"
            binding.tvExpenseDate.text = expense.date
        }
    }
}
