package com.example.tez

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tez.databinding.FragmentAnalysisBinding
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.utils.ColorTemplate

class AnalysisFragment : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchExpensesFromFirestore() // Firebase'den verileri çek
    }

    private fun fetchExpensesFromFirestore() {
        if (userId.isEmpty()) return

        firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .get()
            .addOnSuccessListener { result ->
                val expenseMap = mutableMapOf<String, Float>()
                var totalAmount = 0f

                for (document in result) {
                    val category = document.getString("category") ?: "Diğer"
                    val amount = document.getDouble("amount")?.toFloat() ?: 0f

                    // Kategorileri toplam harcamalara göre grupluyoruz
                    expenseMap[category] = expenseMap.getOrDefault(category, 0f) + amount
                    totalAmount += amount
                }

                updatePieChart(expenseMap, totalAmount)
            }
            .addOnFailureListener { exception ->
                println("Veri alınamadı: ${exception.message}")
            }
    }

    private fun updatePieChart(expenseMap: Map<String, Float>, totalAmount: Float) {
        val pieEntries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        // Kategorilere özel renkler için bir liste
        val predefinedColors = listOf(
            Color.rgb(255, 99, 132),  // Pembe
            Color.rgb(54, 162, 235),  // Mavi
            Color.rgb(255, 206, 86),  // Sarı
            Color.rgb(75, 192, 192),  // Turkuaz
            Color.rgb(153, 102, 255), // Mor
            Color.rgb(255, 159, 64),  // Turuncu
            Color.rgb(233, 30, 99),   // Koyu Pembe
            Color.rgb(66, 133, 244),  // Google Mavisi
            Color.rgb(244, 180, 0),   // Hardal Sarısı
            Color.rgb(15, 157, 88),   // Yeşil
            Color.rgb(255, 87, 34),   // Koyu Turuncu
            Color.rgb(0, 150, 136),   // Turkuaz-Yeşil
            Color.rgb(121, 85, 72),   // Kahverengi
            Color.rgb(63, 81, 181),   // Koyu Mavi
            Color.rgb(33, 150, 243)   // Açık Mavi
        )

        val categoryColors = mutableMapOf<String, Int>()
        var colorIndex = 0

        for ((category, amount) in expenseMap) {
            val percentage = (amount / totalAmount) * 100
            pieEntries.add(PieEntry(percentage, category))

            // Eğer kategori için daha önce renk atanmadıysa yeni bir renk ata
            if (!categoryColors.containsKey(category)) {
                categoryColors[category] = predefinedColors[colorIndex % predefinedColors.size]
                colorIndex++
            }
            colors.add(categoryColors[category]!!)
        }

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.colors = colors // Kategoriye özel renkler eklendi
        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.valueTextSize = 12f

        pieDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${String.format("%.1f", value)}%" // Yüzdelik gösterim
            }
        }


        val pieData = PieData(pieDataSet)
        pieData.setValueTextSize(14f)
        pieData.setValueTextColor(Color.BLACK)

        binding.pieChart.data = pieData
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
