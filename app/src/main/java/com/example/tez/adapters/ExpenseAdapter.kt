package com.example.tez.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.R
import com.example.tez.databinding.ItemExpenseBinding
import com.example.tez.model.Expense
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter(private val expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        val iconResId = categoryIconMap[expense.category] ?: R.drawable.ic_launcher_foreground
        holder.binding.apply {
            ivExpenseIcon.setImageResource(iconResId)
            tvExpenseName.text = expense.name
            tvExpenseCategory.text = expense.category
            tvExpenseAmount.text = "- ${expense.amount} ₺"
            // Date'ı Timestamp'ten Date'e çevirip formatla
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(expense.date.toDate()) // toDate() kullanarak Timestamp'i Date'e çeviriyoruz.
            tvExpenseDate.text = formattedDate

        }
    }

    override fun getItemCount() = expenseList.size

    private val categoryIconMap = mapOf(
        "Restaurant" to R.drawable.ic_restaurant,
        "Car" to R.drawable.ic_car,
        "Groceries" to R.drawable.ic_grocery_store,
        "Transport" to R.drawable.ic_transport,
        "Health" to R.drawable.ic_health,
        "Travel" to R.drawable.ic_travel,
        "Shopping" to R.drawable.ic_shopping,
        "House" to R.drawable.ic_home,
        "Education" to R.drawable.ic_school,
        "Rent" to R.drawable.ic_rent_car,
        "Fun" to R.drawable.ic_fun,
        "Pet" to R.drawable.ic_pet,
        "Other" to R.drawable.ic_add
        )
}

