package com.example.tez

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tez.adapter.BillAdapter
import com.example.tez.databinding.FragmentBillsBinding
import com.example.tez.model.Bill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BillsFragment : Fragment() {

    private var _binding: FragmentBillsBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var billAdapter: BillAdapter
    private var billList = mutableListOf<Bill>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBillsBinding.inflate(inflater, container, false)

        billAdapter = BillAdapter(
            billList,
            onEditClick = { bill -> editBill(bill) },
            onConfirmClick = { bill -> confirmBill(bill) },
            onAmountChanged = { bill -> updateBillAmountInFirestore(bill) }
        )


        binding.rvBills.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBills.adapter = billAdapter

        fetchBillsFromFirestore()

        binding.imgBtnAddBill.setOnClickListener {
            findNavController().navigate(BillsFragmentDirections.actionBillsFragmentToAddBillFragment())
        }

        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkAndCreateNewMonthBills()
        binding.toolbarBills.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_billsFragment_to_homeFragment)
        }
    }

    // Firestore'a güncellenen miktarı kaydetme fonksiyonu
    private fun updateBillAmountInFirestore(bill: Bill) {
        if (userId == null) return

        val billRef = firestore.collection("users").document(userId)
            .collection("bills").document(bill.id) // 'id' üzerinden güncelleme yapıyoruz

        billRef.update("amount", bill.amount)
            .addOnSuccessListener {
                Log.d("BillUpdate", "Fatura başarıyla güncellendi.")
            }
            .addOnFailureListener { e ->
                Log.e("BillUpdate", "Güncelleme başarısız: ${e.message}")
            }
    }


    private fun fetchBillsFromFirestore() {
        if (userId == null) return

        val currentMonth = Bill.getCurrentMonth()
        val billsCollection = firestore.collection("users").document(userId).collection("bills")

        billsCollection
            .whereEqualTo("month", currentMonth)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                billList.clear()
                for (document in snapshot.documents) {
                    val bill = document.toObject(Bill::class.java)?.copy(id = document.id)
                    if (bill != null) {
                        billList.add(bill)
                    }
                }
                billAdapter.notifyDataSetChanged()
            }
    }


    private fun updateBillStatusInFirestore(bill: Bill) {
        if (userId == null) return

        val billRef = firestore.collection("users").document(userId)
            .collection("bills").document(bill.id) // 'id' üzerinden güncelleme yapıyoruz

        billRef.update("status", bill.status)
            .addOnSuccessListener {
                Log.d("BillUpdate", "Fatura durumu başarıyla güncellendi.")
                Toast.makeText(requireContext(), "${bill.name} faturası ödendi olarak işaretlendi.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("BillUpdate", "Güncelleme başarısız: ${e.message}")
                Toast.makeText(requireContext(), "Fatura durumu güncellenirken hata oluştu.", Toast.LENGTH_SHORT).show()
            }
    }



    private fun editBill(bill: Bill) {
        // Buraya düzenleme işlemi için kod eklenebilir
    }

    private fun confirmBill(bill: Bill) {
        bill.status = "Paid"
        updateBillStatusInFirestore(bill)
        billAdapter.notifyDataSetChanged()
    }

    private fun checkAndCreateNewMonthBills() {
        if (userId == null) return

        val currentMonth = Bill.getCurrentMonth()
        val billsRef = firestore.collection("users").document(userId).collection("bills")

        billsRef.get().addOnSuccessListener { snapshot ->
            val allBills = snapshot.documents.mapNotNull { doc ->
                val bill = doc.toObject(Bill::class.java)
                if (bill != null) bill to doc.id else null
            }


            // Eğer bu ayın faturaları zaten varsa, işlem yapma
            if (allBills.any { it.first.month == currentMonth }) return@addOnSuccessListener

            // Son ayın fatura verilerini bul
            val previousMonthBills = allBills
                .groupBy { it.first.month }
                .maxByOrNull { it.key }?.value ?: return@addOnSuccessListener

            for ((bill, _) in previousMonthBills) {
                val newBill = Bill(
                    name = bill.name,
                    amount = bill.amount,
                    month = currentMonth,
                    status = "Unpaid",
                    iconResId = bill.iconResId
                )
                billsRef.add(newBill)
                    .addOnSuccessListener {
                        Log.d("BillFirestore", "Yeni ay faturası eklendi: ${newBill.name}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("BillFirestore", "Fatura eklenemedi: ${e.message}")
                    }

            }
        }
    }


}