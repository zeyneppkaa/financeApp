package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tez.adapters.CategoryAdapter
//import com.example.tez.adapters.TestExpenseAdapter
import com.example.tez.databinding.FragmentExpensesBinding
import com.example.tez.model.Category
import com.example.tez.model.Expense

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var testExpensesAdapter: TestExpenseAdapter
    private val selectedExpenses = mutableListOf<Expense>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        val view = binding.root

        val categories = listOf(
            Category("Restaurant", R.drawable.ic_restaurant),
            Category("Car", R.drawable.ic_car),
            Category("Groceries", R.drawable.ic_grocery_store),
            Category("Transport", R.drawable.ic_launcher_foreground),
            Category("Health", R.drawable.ic_health),
            Category("Travel", R.drawable.ic_travel),
            Category("Shopping", R.drawable.ic_launcher_foreground),
            Category("House", R.drawable.ic_home),
            Category("Education", R.drawable.ic_school),
            Category("Rent", R.drawable.ic_launcher_foreground),
            Category("Fun", R.drawable.ic_launcher_foreground),
            Category("Pet", R.drawable.ic_launcher_foreground)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            binding.expenseInputLayout.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE
            binding.ivCategoryIcon.setImageResource(category.iconRes) // Seçilen kategorinin ikonunu göster
        }

        binding.rvCategories.layoutManager = GridLayoutManager(context, 3)
        binding.rvCategories.adapter = categoryAdapter

        // TEST VERİLERİ: Hardcoded harcamalar
        val testExpenses = listOf(
            Expense("Restaurant", "Lunch", 50.0, "2025-02-24"),
            Expense("Transport", "Bus Ticket", 20.0, "2025-02-23"),
            Expense("Groceries", "Apple", 5.0, "2025-02-22"),
            Expense("Health", "Doctor Appointment", 100.0, "2025-02-21")
        )

        // TEST VERİLERİNİ LİSTEYE EKLE
        selectedExpenses.addAll(testExpenses)

        // RecyclerView için adapter tanımla
        testExpensesAdapter = TestExpenseAdapter(selectedExpenses)
        binding.rvTestExpenses.layoutManager = GridLayoutManager(context, 1)
        binding.rvTestExpenses.adapter = testExpensesAdapter

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString()
            val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val selectedCategory = categoryAdapter.getSelectedCategory()

            if (selectedCategory != null && name.isNotEmpty() && price > 0) {
                val expense = Expense(
                    category = selectedCategory.name,
                    name = name,
                    price = price,
                    date = "2025-02-24"
                )
                selectedExpenses.add(expense)
                testExpensesAdapter.notifyDataSetChanged()

                binding.etName.text.clear()
                binding.etPrice.text.clear()
                binding.expenseInputLayout.visibility = View.GONE
                binding.btnSave.visibility = View.GONE

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
