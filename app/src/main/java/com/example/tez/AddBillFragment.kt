package com.example.tez

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.adapters.CategoryAdapter
import com.example.tez.adapters.SelectionMode
import com.example.tez.databinding.FragmentAddBillBinding
import com.example.tez.model.Bill
import com.example.tez.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddBillFragment : Fragment() {

    private var _binding: FragmentAddBillBinding? = null
    private val binding get() = _binding!!

    private lateinit var billCategoryAdapter: CategoryAdapter
    private lateinit var billCategoryList: List<Category>
    private val selectedCategories = mutableSetOf<Category>()

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBillBinding.inflate(inflater, container, false)

        billCategoryList = listOf(
            Category("Electricity", R.drawable.ic_electricity),
            Category("Water", R.drawable.ic_water),
            Category("Internet", R.drawable.ic_internet),
            Category("Natural Gas", R.drawable.ic_natural_gas),
            Category("Mobile Bill", R.drawable.ic_mobile_bill),
            Category("Netflix", R.drawable.ic_netflix),
            Category("Spotify", R.drawable.ic_spotify),
            Category("Disney Plus", R.drawable.ic_disney_plus),
            Category("Youtube Music", R.drawable.ic_youtube_music),
            Category("Blu TV", R.drawable.ic_blutv)
        )

        billCategoryAdapter = CategoryAdapter(billCategoryList, SelectionMode.MULTIPLE) { selectedCategories ->
            updateSelectedBills(selectedCategories)
        }

        val recyclerView: RecyclerView = binding.rvBillCategories
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = billCategoryAdapter

        loadSelectedCategories() // Firestore'dan seçili kategorileri al

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            val action = AddBillFragmentDirections.actionAddBillFragmentToBillsFragment()
            findNavController().navigate(action)
        }
    }

    private fun loadSelectedCategories() {
        if (userId == null) return

        firestore.collection("users").document(userId).collection("bills")
            .get()
            .addOnSuccessListener { result ->
                selectedCategories.clear()
                for (document in result) {
                    val name = document.getString("name") ?: continue
                    val iconResId = document.getLong("iconResId")?.toInt() ?: continue
                    selectedCategories.add(Category(name, iconResId))
                }
                billCategoryAdapter.setSelectedCategories(selectedCategories) // Adapter'a güncellenmiş seçili kategorileri aktar
            }
    }

    private fun updateSelectedBills(selectedCategories: List<Category>) {
        Log.d("UPDATE_BILL", "updateSelectedBills çağrıldı. Seçilen kategori sayısı: ${selectedCategories.size}")
        if (userId == null) return

        val userRef = firestore.collection("users").document(userId)
        val billsCollection = userRef.collection("bills")

        // Firestore'daki mevcut faturaları al
        billsCollection.get()
            .addOnSuccessListener { result ->
                val existingCategories = result.documents.mapNotNull { it.getString("name") }.toSet()

                // Yeni eklenen kategorileri ekle
                selectedCategories.forEach { category ->
                    if (category.name !in existingCategories) {
                        val bill = Bill(
                            name = category.name,
                            amount = 0.0,
                            month = Bill.getCurrentMonth(),
                            status = "Unpaid",
                            iconResId = category.iconRes
                        )
                        billsCollection.add(bill)
                            .addOnSuccessListener {
                                Log.d("AddBillFragment", "${bill.name} Firestore'a eklendi")
                            }
                            .addOnFailureListener {
                                Log.e("AddBillFragment", "Firestore'a eklenemedi: ${it.message}")
                            }
                    }
                }

                // Firestore'dan kaldırılması gerekenleri sil
                result.documents.forEach { document ->
                    val name = document.getString("name") ?: return@forEach
                    val month = document.getString("month") ?: return@forEach

                    // Eğer bu kategori mevcut değilse veya kategori istenmiyorsa sil
                    if (name !in selectedCategories.map { it.name } && month == Bill.getCurrentMonth()) {
                        billsCollection.document(document.id).delete()
                    }
                }
            }
    }
    /* private fun saveSelectedBillsToFirestore(selectedCategories: List<Category>) {
        if (userId == null) {
            Toast.makeText(requireContext(), "Kullanıcı kimliği alınamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = firestore.collection("users").document(userId)
        val billsCollection = userRef.collection("bills")

        selectedCategories.forEach { category ->
            val bill = Bill(
                name = category.name,
                amount = 0.0,
                month = Bill.getCurrentMonth(),
                status = "Unpaid",
                iconResId = category.iconRes
            )

            billsCollection.add(bill)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "${category.name} faturası eklendi", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Fatura eklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                }
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
