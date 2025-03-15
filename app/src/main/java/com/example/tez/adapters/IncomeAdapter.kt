package com.example.tez.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.R
import com.example.tez.databinding.ItemIncomeBinding
import com.example.tez.model.Incomes
import java.text.SimpleDateFormat
import java.util.Locale

class IncomeAdapter(private val incomeList: List<Incomes>) :
   RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

   class IncomeViewHolder(val binding: ItemIncomeBinding) : RecyclerView.ViewHolder(binding.root)

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
      val binding = ItemIncomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
      return IncomeViewHolder(binding)
   }

   override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
      val income = incomeList[position]
      val iconResId = categoryIconMap[income.category] ?: R.drawable.ic_launcher_foreground
      holder.binding.apply {
         ivIncomeIcon.setImageResource(iconResId)
         tvIncomeName.text = income.name
         tvIncomeCategory.text = income.category
         tvIncomeAmount.text = "+ ${income.amount} ₺"
         // Date'ı Timestamp'ten Date'e çevirip formatla
         val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
         val formattedDate = dateFormat.format(income.date.toDate()) // toDate() kullanarak Timestamp'i Date'e çeviriyoruz.
         tvIncomeDate.text = formattedDate

      }
   }

   override fun getItemCount() = incomeList.size

   private val categoryIconMap = mapOf(
      "Salary" to R.drawable.baseline_work_24,
      "Investment" to R.drawable.baseline_monetization_on_24,
      "Freelance" to R.drawable.baseline_computer_24,
      "Rental" to R.drawable.baseline_other_houses_24,
      "Bonus" to R.drawable.baseline_add_card_24,
      "Crypto" to R.drawable.baseline_enhanced_encryption_24,
      "Gift" to R.drawable.baseline_card_giftcard_24,
      "Refund" to R.drawable.baseline_refresh_24,
      "Other" to R.drawable.ic_add
   )
}



