package com.example.tez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tez.adapters.CategoryAdapter
import com.example.tez.adapters.ExpenseAdapter
import com.example.tez.adapters.SelectionMode
import com.example.tez.databinding.FragmentExpensesBinding
import com.example.tez.model.Category
import com.example.tez.model.Expense
import com.example.tez.utils.FilterDropdownHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import java.security.Timestamp
import java.util.Calendar
import java.util.Date

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var adapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var selectedCategory: Category? = null
    private var selectedFilter: String = "See All  v"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        val view = binding.root

        // Kategoriler
        val categories = listOf(
            Category("Restaurant", R.drawable.ic_restaurant),
            Category("Car", R.drawable.ic_car),
            Category("Groceries", R.drawable.ic_grocery_store),
            Category("Transport", R.drawable.ic_transport),
            Category("Health", R.drawable.ic_health),
            Category("Travel", R.drawable.ic_travel),
            Category("Shopping", R.drawable.ic_shopping),
            Category("House", R.drawable.ic_home),
            Category("Education", R.drawable.ic_school),
            Category("Rent", R.drawable.ic_rent_car),
            Category("Fun", R.drawable.ic_fun),
            Category("Pet", R.drawable.ic_pet),
            Category("Other", R.drawable.ic_add)

        )

        categoryAdapter = CategoryAdapter(categories, SelectionMode.SINGLE) { selectedCategories ->
            selectedCategory = selectedCategories.firstOrNull()
            updateUIBasedOnSelection()
        }

        binding.rvCategories.layoutManager = GridLayoutManager(context, 4)
        binding.rvCategories.adapter = categoryAdapter

        binding.tvSeeAll.text = selectedFilter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchExpensesFromFirestore()

        val filterOptions =
            listOf("See All", "Last 7 days", "This month", "Last 1 month", "Last 3 months")

        binding.tvSeeAll.setOnClickListener {
            FilterDropdownHelper(
                requireContext(),
                binding.tvSeeAll,
                filterOptions
            ) { selectedFilter ->
                applyFilter(selectedFilter)
            }.showFilterMenu()
        }

        binding.imgBtnCamera.setOnClickListener {
            val bundle = Bundle().apply {
                putString("selectedCategoryName", selectedCategory?.name)
            }
            findNavController().navigate(R.id.action_expensesFragment_to_scanFragment, bundle)
        }


        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val amount = binding.etPrice.text.toString().trim()

            if (selectedCategory != null && name.isNotEmpty() && amount.isNotEmpty()) {
                val expense = Expense(
                    amount = amount.toDouble(),
                    category = selectedCategory!!.name,
                    name = name
                )

                addExpenseToFirestore(expense)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Lütfen tüm bilgileri giriniz!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.toolbarExpenses.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_expensesFragment_to_homeFragment)
        }
    }

    private fun updateUIBasedOnSelection() {
        if (selectedCategory == null) {
            binding.expenseInputLayout.visibility = View.GONE
            binding.btnSave.visibility = View.GONE
            binding.imgBtnCamera.visibility = View.GONE
        } else {
            binding.expenseInputLayout.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE
            binding.imgBtnCamera.visibility = View.VISIBLE
            binding.ivCategoryIcon.setImageResource(selectedCategory!!.iconRes)
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(expenseList)
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = false
            stackFromEnd = false
        }
        binding.rvExpenses.adapter = adapter
    }

    private fun fetchExpensesFromFirestore(filter: String = "See All  v") {
        if (userId.isEmpty()) return

        val expensesRef = firestore.collection("users")
            .document(userId)
            .collection("expenses")

        val query: Query = when (filter) {
            "Last 7 days" -> expensesRef.whereGreaterThan("date", getPastDate(7))
            "This month" -> expensesRef.whereGreaterThan("date", getMonthStart())
            "Last 1 month" -> expensesRef.whereGreaterThan("date", getPastDate(30))
            "Last 3 months" -> expensesRef.whereGreaterThan("date", getPastDate(90))
            else -> expensesRef
        }.orderBy("date", Query.Direction.DESCENDING)

        query.get()
            .addOnSuccessListener { result ->
                expenseList.clear()
                for (document in result) {
                    val expense = document.toObject(Expense::class.java)
                    expenseList.add(expense)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Veri alınamadı: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun applyFilter(filter: String) {
        selectedFilter = filter
        binding.tvSeeAll.text = selectedFilter
        fetchExpensesFromFirestore(selectedFilter)
    }

    private fun getPastDate(days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.time
    }

    private fun getMonthStart(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.time
    }



    private fun addExpenseToFirestore(expense: Expense) {
        if (userId.isEmpty()) return
        val expenseWithTimestamp = expense.copy(date = com.google.firebase.Timestamp.now())

        val expenseRef: DocumentReference = firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .document()

        expenseRef.set(expenseWithTimestamp)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Gider başarıyla eklendi!", Toast.LENGTH_SHORT)
                    .show()

                // Ekledikten sonra listeyi güncelle
                expenseList.add(0, expenseWithTimestamp)
                adapter.notifyItemInserted(0)
                binding.rvExpenses.scrollToPosition(0)

                // Formu temizle
                binding.etName.text.clear()
                binding.etPrice.text.clear()
                binding.expenseInputLayout.visibility = View.GONE
                binding.btnSave.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Gider eklenemedi: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
