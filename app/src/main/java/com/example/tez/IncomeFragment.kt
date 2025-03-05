package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tez.adapters.CategoryAdapter
import com.example.tez.adapters.IncomeAdapter
import com.example.tez.databinding.FragmentIncomeBinding
import com.example.tez.model.Category
import com.example.tez.model.Incomes


class IncomeFragment : Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var IncomeAdapter: IncomeAdapter
    private val selectedIncomes = mutableListOf<Incomes>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val categories = listOf(
            Category("Salary", R.drawable.baseline_work_24),
            Category("Investment", R.drawable.baseline_monetization_on_24),
            Category("Freelance", R.drawable.baseline_computer_24),
            Category("Rental ", R.drawable.baseline_other_houses_24),
            Category("Bonus", R.drawable.baseline_add_card_24),
            Category("Crypto", R.drawable.baseline_enhanced_encryption_24),
            Category("Gift", R.drawable.baseline_card_giftcard_24),
            Category("Refund", R.drawable.baseline_refresh_24),
            Category("Other", R.drawable.ic_add),

        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            binding.incomeInputLayout.visibility = View.VISIBLE
            binding.btnSaveIncome.visibility = View.VISIBLE
            binding.ivCategoryIcon.setImageResource(category.iconRes) // Seçilen kategorinin ikonunu göster
        }

        binding.rvIncomeCategories.layoutManager = GridLayoutManager(context, 3)
        binding.rvIncomeCategories.adapter = categoryAdapter

        // TEST VERİLERİ: Hardcoded harcamalar
        val testExpenses = listOf(
            Incomes("Salary", "Job", 3750.0, "2025-01-05"),
            Incomes("Salary", "Pension", 1020.0, "2025-01-20"),
            Incomes("Investment", "Bitcoin", 565.0, "2025-02-22"),
            Incomes("Rental", "Summer House", 1100.0, "2025-02-26")
        )

        // TEST VERİLERİNİ LİSTEYE EKLE
        selectedIncomes.addAll(testExpenses)

        // RecyclerView için adapter tanımla
        IncomeAdapter = IncomeAdapter(selectedIncomes)
        binding.rvTestIncome.layoutManager = GridLayoutManager(context, 1)
        binding.rvTestIncome.adapter = IncomeAdapter

        binding.btnSaveIncome.setOnClickListener {
            val name = binding.incomeName.text.toString()
            val price = binding.incomePrice.text.toString().toDoubleOrNull() ?: 0.0
            val selectedCategory = categoryAdapter.getSelectedCategory()

            if (selectedCategory != null && name.isNotEmpty() && price > 0) {
                val incomes = Incomes(
                    category1 = selectedCategory.name,
                    name1 = name,
                    price1 = price,
                    date1 = "2025-02-24"
                )
                selectedIncomes.add(incomes)
                IncomeAdapter.notifyDataSetChanged()

                binding.incomeName.text.clear()
                binding.incomePrice.text.clear()
                binding.incomeInputLayout.visibility = View.GONE
                binding.btnSaveIncome.visibility = View.GONE

                categoryAdapter.clearSelectedCategory()
            }
        }

        return view
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}