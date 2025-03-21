package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tez.adapters.CategoryAdapter
import com.example.tez.adapters.IncomeAdapter
import com.example.tez.databinding.FragmentIncomeBinding
import com.example.tez.model.Category
import com.example.tez.model.Incomes
import com.example.tez.utils.FilterDropdownHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.Date


class IncomeFragment : Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var incomeAdapter: IncomeAdapter
    private val incomeList = mutableListOf<Incomes>()

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var selectedCategory: Category? = null
    private var selectedFilter: String = "See All  v"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Kategoriler
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
            if (category == null) {
                selectedCategory = null
                binding.incomeInputLayout.visibility = View.GONE
                binding.btnSaveIncome.visibility = View.GONE
            } else {
                selectedCategory = category
                binding.incomeInputLayout.visibility = View.VISIBLE
                binding.btnSaveIncome.visibility = View.VISIBLE
                binding.ivCategoryIcon.setImageResource(category.iconRes)
            }
        }

        binding.rvIncomeCategories.layoutManager = GridLayoutManager(context, 4)
        binding.rvIncomeCategories.adapter = categoryAdapter

        binding.tvSeeAll.text = selectedFilter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView kurulumunu yap
        setupRecyclerView()
        fetchIncomesFromFirestore()

        val filterOptions = listOf("See All", "Last 7 days", "This month", "Last 1 month", "Last 3 months")

        binding.tvSeeAll.setOnClickListener {
            FilterDropdownHelper(requireContext(), binding.tvSeeAll, filterOptions) { selectedFilter ->
                applyFilter(selectedFilter)
            }.showFilterMenu()
        }

        binding.btnSaveIncome.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val amount = binding.etPrice.text.toString().trim()
            //val date = com.google.firebase.firestore.Timestamp(Date()) // Şu anki tarih ve saat

            if (selectedCategory != null && name.isNotEmpty() && amount.isNotEmpty()) {
                val income = Incomes(
                    amount = amount.toDouble(),
                    category = selectedCategory!!.name,
                    //date = date,
                    name = name
                )

                // Veriyi Firestore'a kaydet
                addIncomeToFirestore(income)
            } else {
                Toast.makeText(requireContext(), "Lütfen tüm bilgileri giriniz!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.toolbarIncome.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_incomeFragment_to_homeFragment)
        }
    }

    private fun setupRecyclerView() {
        incomeAdapter = IncomeAdapter(incomeList)
        binding.rvIncomes.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = false
            stackFromEnd = false
        }
        binding.rvIncomes.adapter = incomeAdapter
    }

    private fun fetchIncomesFromFirestore(filter: String = "See All  v") {
        if (userId.isEmpty()) return

        val incomesRef = firestore.collection("users")
            .document(userId)
            .collection("incomes")

        val query: Query = when (filter) {
            "Last 7 days" -> incomesRef.whereGreaterThan("date", getPastDate(7))
            "This month" -> incomesRef.whereGreaterThan("date", getMonthStart())
            "Last 1 month" -> incomesRef.whereGreaterThan("date", getPastDate(30))
            "Last 3 months" -> incomesRef.whereGreaterThan("date", getPastDate(90))
            else -> incomesRef
        }.orderBy("date", Query.Direction.DESCENDING)

        query.get()
            .addOnSuccessListener { result ->
                incomeList.clear()
                for (document in result) {
                    val expense = document.toObject(Incomes::class.java)
                    incomeList.add(expense)
                }
                incomeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Veri alınamadı: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyFilter(filter: String) {
        selectedFilter = filter
        binding.tvSeeAll.text = selectedFilter
        fetchIncomesFromFirestore(selectedFilter)
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

    private fun addIncomeToFirestore(income: Incomes) {
        if (userId.isEmpty()) return
        val incomeWithTimestamp = income.copy(date = com.google.firebase.Timestamp.now())

        val expenseRef: DocumentReference = firestore.collection("users")
            .document(userId)
            .collection("incomes")
            .document() // Yeni bir belge ekliyoruz

        expenseRef.set(incomeWithTimestamp)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Gider başarıyla eklendi!", Toast.LENGTH_SHORT).show()

                // Ekledikten sonra listeyi güncelle
                incomeList.add(0, incomeWithTimestamp)
                incomeAdapter.notifyItemInserted(0)
                binding.rvIncomes.scrollToPosition(0)

                // Formu temizle
                binding.etName.text.clear()
                binding.etPrice.text.clear()
                binding.incomeInputLayout.visibility = View.GONE
                binding.btnSaveIncome.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Gider eklenemedi: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}