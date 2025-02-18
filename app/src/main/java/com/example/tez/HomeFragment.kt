package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val navigationHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //val navController = navigationHost.navController

        val btnExpenses = view.findViewById<Button>(R.id.btn_expenses)
        btnExpenses.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_expensesFragment)
        }
    }

/*        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val btnExpenses = view.findViewById<Button>(R.id.btn_expenses)

        view.findViewById<Button>(R.id.btn_income).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_income)
        }
        btnExpenses.setOnClickListener {
            Navigation.findNavController(view).navigate(com.google.android.material.R.id.action_bar)
        }

        view.findViewById<Button>(R.id.btn_bills).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_bills)
        }
        view.findViewById<Button>(R.id.btn_accounts).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_accounts)
        }
        view.findViewById<Button>(R.id.btn_analysis).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_analysis)
        }*/





}