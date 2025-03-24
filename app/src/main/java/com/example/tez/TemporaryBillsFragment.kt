package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.tez.databinding.FragmentTemporaryBillsBinding

class TemporaryBillsFragment : Fragment() {

    private var _binding: FragmentTemporaryBillsBinding? = null
    private val binding get()= _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTemporaryBillsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddBills.setOnClickListener {
            val action = TemporaryBillsFragmentDirections.actionTemporaryBillsFragmentToAddBillFragment()
            findNavController().navigate(action)
        }

    }

}