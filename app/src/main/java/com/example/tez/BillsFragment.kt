package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        billAdapter = BillAdapter(billList,
            onEditClick = { bill -> editBill(bill) },
            onConfirmClick = { bill -> confirmBill(bill) }
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

        binding.toolbarBills.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_billsFragment_to_homeFragment)
        }
    }

    private fun fetchBillsFromFirestore() {
        if (userId == null) return

        val billsCollection = firestore.collection("users").document(userId).collection("bills")

        billsCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            billList.clear()
            for (document in snapshot.documents) {
                val bill = document.toObject(Bill::class.java)
                if (bill != null) {
                    billList.add(bill)
                }
            }
            billAdapter.notifyDataSetChanged()
        }
    }

    private fun editBill(bill: Bill) {
        // Buraya düzenleme işlemi için kod eklenebilir
    }

    private fun confirmBill(bill: Bill) {
        // Ödeme onayı için kod eklenebilir
    }

}