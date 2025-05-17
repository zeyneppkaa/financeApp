package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.tez.databinding.FragmentChangePasswordBinding
import com.example.tez.databinding.FragmentSecurityProfileBinding


class SecurityProfileFragment : Fragment() {



    private var _binding: FragmentSecurityProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.leftSecurity.setOnClickListener() {
            findNavController().navigate(R.id.action_securityProfileFragment_to_profileFragment)
        }
    }


}