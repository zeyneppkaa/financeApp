package com.example.tez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tez.adapters.CategoryAdapter
import com.example.tez.adapters.TestExpenseAdapter
import com.example.tez.databinding.FragmentExpensesBinding
import com.example.tez.model.Category
import com.example.tez.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import java.text.SimpleDateFormat

import java.util.*

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var adapter: TestExpenseAdapter
    private val expenseList = mutableListOf<Expense>()

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var selectedCategory: Category? = null

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

        // Kategori Adapter
        categoryAdapter = CategoryAdapter(categories) { category ->
            selectedCategory = category
            binding.expenseInputLayout.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE
            binding.ivCategoryIcon.setImageResource(category.iconRes) // Seçilen kategori ikonunu göster
        }

        binding.rvCategories.layoutManager = GridLayoutManager(context, 3)
        binding.rvCategories.adapter = categoryAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView kurulumunu yap
        setupRecyclerView()

        // Firestore'dan verileri al
        fetchExpensesFromFirestore()

        // Save butonuna tıklama işlemi
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val amount = binding.etPrice.text.toString().trim()
            //val date = com.google.firebase.firestore.Timestamp(Date()) // Şu anki tarih ve saat

            if (selectedCategory != null && name.isNotEmpty() && amount.isNotEmpty()) {
                val expense = Expense(
                    amount = amount.toDouble(),
                    category = selectedCategory!!.name,
                    //date = date,
                    name = name
                )

                // Veriyi Firestore'a kaydet
                addExpenseToFirestore(expense)
            } else {
                Toast.makeText(requireContext(), "Lütfen tüm bilgileri giriniz!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TestExpenseAdapter(expenseList)
        binding.rvTestExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTestExpenses.adapter = adapter
    }

    private fun fetchExpensesFromFirestore() {
        if (userId.isEmpty()) return

        firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .get()
            .addOnSuccessListener { result ->
                expenseList.clear()
                for (document in result) {
                    val expense = document.toObject(Expense::class.java)
                    expenseList.add(expense)
                }
                adapter.notifyDataSetChanged() // Veriler başarıyla alındığında RecyclerView'u güncelle
            }
            .addOnFailureListener { exception ->
                // Hata yönetimi (örneğin: hata mesajı göstermek)
                Toast.makeText(requireContext(), "Veri alınamadı: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addExpenseToFirestore(expense: Expense) {
        if (userId.isEmpty()) return

        val expenseRef: DocumentReference = firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .document() // Yeni bir belge ekliyoruz

        expenseRef.set(expense)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Gider başarıyla eklendi!", Toast.LENGTH_SHORT).show()

                // Ekledikten sonra listeyi güncelle
                expenseList.add(expense)
                adapter.notifyItemInserted(expenseList.size - 1)

                // Formu temizle
                binding.etName.text.clear()
                binding.etPrice.text.clear()
                binding.expenseInputLayout.visibility = View.GONE
                binding.btnSave.visibility = View.GONE
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
