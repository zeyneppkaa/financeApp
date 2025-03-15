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
import com.example.tez.model.Expense
import com.example.tez.model.Incomes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class IncomeFragment : Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var incomeAdapter: IncomeAdapter
    private val incomeList = mutableListOf<Incomes>()

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var selectedCategory: Category? = null

    //private val selectedIncomes = mutableListOf<Incomes>()

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
            selectedCategory = category
            binding.incomeInputLayout.visibility = View.VISIBLE
            binding.btnSaveIncome.visibility = View.VISIBLE
            binding.ivCategoryIcon.setImageResource(category.iconRes) // Seçilen kategorinin ikonunu göster
        }

        binding.rvIncomeCategories.layoutManager = GridLayoutManager(context, 4)
        binding.rvIncomeCategories.adapter = categoryAdapter

       /* // TEST VERİLERİ: Hardcoded harcamalar
        val testExpenses = listOf(
            Incomes("Salary", "Job", 3750.0, "2025-01-05"),
            Incomes("Salary", "Pension", 1020.0, "2025-01-20"),
            Incomes("Investment", "Bitcoin", 565.0, "2025-02-22"),
            Incomes("Rental", "Summer House", 1100.0, "2025-02-26")
        )

        // TEST VERİLERİNİ LİSTEYE EKLE
        selectedIncomes.addAll(testExpenses)

        // RecyclerView için adapter tanımla
        incomeAdapter = IncomeAdapter(selectedIncomes)
        binding.rvTestIncome.layoutManager = GridLayoutManager(context, 1)
        binding.rvTestIncome.adapter = incomeAdapter

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
                incomeAdapter.notifyDataSetChanged()

                binding.incomeName.text.clear()
                binding.incomePrice.text.clear()
                binding.incomeInputLayout.visibility = View.GONE
                binding.btnSaveIncome.visibility = View.GONE

                categoryAdapter.clearSelectedCategory()
            }
        }*/

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView kurulumunu yap
        setupRecyclerView()

        // Firestore'dan verileri al
        fetchExpensesFromFirestore()

        // Save butonuna tıklama işlemi
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
            reverseLayout = false // 🔹 Doğru sıralama için
            stackFromEnd = false  // 🔹 Listenin başından başlamasını sağla
        }
        binding.rvIncomes.adapter = incomeAdapter
    }

    private fun fetchExpensesFromFirestore() {
        if (userId.isEmpty()) return

        firestore.collection("users")
            .document(userId)
            .collection("incomes")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                incomeList.clear()
                for (document in result) {
                    val income = document.toObject(Incomes::class.java)
                    incomeList.add(income)
                }
                incomeAdapter.notifyDataSetChanged() // Veriler başarıyla alındığında RecyclerView'u güncelle
            }
            .addOnFailureListener { exception ->
                // Hata yönetimi (örneğin: hata mesajı göstermek)
                Toast.makeText(requireContext(), "Veri alınamadı: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
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
                incomeList.add(0, incomeWithTimestamp) // ✅ En üste ekle
                incomeAdapter.notifyItemInserted(0)
                binding.rvIncomes.scrollToPosition(0) // ✅ Yeni eklenen öğeye kaydır

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