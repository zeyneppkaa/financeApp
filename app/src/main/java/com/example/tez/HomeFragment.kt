package com.example.tez

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.tez.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get()= _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnExpenses.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToExpensesFragment()
            findNavController().navigate(action)
        }
        binding.btnIncomes.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToIncomeFragment()
            findNavController().navigate(action)
        }

        binding.btnBills.setOnClickListener {
            checkFirstTimeBillsClick()
        }
    }

    private fun checkFirstTimeBillsClick() {
        val userId = auth.currentUser?.uid ?: return
        val sharedPref = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val hasClickedBills = sharedPref.getBoolean("hasClickedBills_$userId", false)

        if (!hasClickedBills) {
            // İlk kez tıklıyorsa TemporaryBillsFragment'e yönlendir
            val action = HomeFragmentDirections.actionHomeFragmentToTemporaryBillsFragment()
            findNavController().navigate(action)

            // Kullanıcının artık butona tıkladığını kaydet
            sharedPref.edit().putBoolean("hasClickedBills_$userId", true).apply()
        } else {
            // Daha önce tıkladıysa BillsFragment'e yönlendir
            val action = HomeFragmentDirections.actionHomeFragmentToBillsFragment()
            findNavController().navigate(action)
        }
    }

}