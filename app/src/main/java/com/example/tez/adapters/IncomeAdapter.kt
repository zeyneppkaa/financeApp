package com.example.tez.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.databinding.ItemIncomeBinding
import com.example.tez.model.Incomes

class IncomeAdapter (

   var incomes1: List<Incomes>
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>()


   {
       inner class IncomeViewHolder(private val binding: ItemIncomeBinding) : RecyclerView.ViewHolder(binding.root){
          fun bind(incomes: Incomes){
             binding.tvIncomeAmount.text = "${incomes.price1} USD"
             binding.tvIncomeDate.text = incomes.date1
             binding.tvIncomeName.text = incomes.name1
             binding.tvIncomeCategory.text = incomes.category1

          }
       }

      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
         val view_income = ItemIncomeBinding.inflate(LayoutInflater.from(parent.context), parent,false)
         return IncomeViewHolder(view_income)
      }

      override fun getItemCount(): Int =  incomes1.size

      override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
         holder.bind(incomes1[position])
         }

      }



