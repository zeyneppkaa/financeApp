package com.example.tez

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tez.databinding.FragmentAnalysisBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        fetchExpensesFromFirestore()
        fetchIncomeFromFirestore()
    }

    private fun fetchExpensesFromFirestore() {
        if (userId.isEmpty()) return

        firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .addSnapshotListener { expenseResult, expenseException ->
                if (expenseException != null) {
                    println("Gider verisi alınamadı: ${expenseException.message}")
                    return@addSnapshotListener
                }

                val expenseMap = mutableMapOf<String, Float>()
                var totalAmount = 0f

                expenseResult?.forEach { document ->
                    val category = document.getString("category") ?: "Diğer"
                    val amount = document.getDouble("amount")?.toFloat() ?: 0f
                    expenseMap[category] = expenseMap.getOrDefault(category, 0f) + amount
                    totalAmount += amount
                }

                //bills ekleniyor
                firestore.collection("users")
                    .document(userId)
                    .collection("bills")
                    .get()
                    .addOnSuccessListener { billsResult ->
                        var totalBillsAmount = 0f
                        billsResult.forEach { billDoc ->
                            val amount = billDoc.getDouble("amount")?.toFloat() ?: 0f
                            totalBillsAmount += amount
                        }

                        if (totalBillsAmount > 0f) {
                            expenseMap["Bills"] = totalBillsAmount
                            totalAmount += totalBillsAmount
                        }

                        updatePieChart(expenseMap, totalAmount)
                    }
                    .addOnFailureListener { e ->
                        println("Fatura verisi alınamadı: ${e.message}")
                        // Faturalar alınamasa da pie chart güncellenir
                        updatePieChart(expenseMap, totalAmount)
                    }
            }
    }

    private fun createCustomLegend(
        expenseMap: Map<String, Float>,
        categoryColors: Map<String, Int>,
        categoryPercentages: Map<String, Float>
    ) {
        binding.customLegendLayout.removeAllViews()
        val inflater = LayoutInflater.from(context)

        for ((category, _) in expenseMap) {
            val legendItem =
                inflater.inflate(R.layout.item_legend, binding.customLegendLayout, false)

            val colorBox = legendItem.findViewById<View>(R.id.colorBox)
            val label = legendItem.findViewById<android.widget.TextView>(R.id.label)
            val percent = legendItem.findViewById<android.widget.TextView>(R.id.percent)

            colorBox.setBackgroundColor(categoryColors[category] ?: Color.GRAY)
            label.text = category

            // Yüzdeyi TextView'e yaz
            percent.text = String.format("%.1f%%", categoryPercentages[category] ?: 0f)

            binding.customLegendLayout.addView(legendItem)
        }
    }

    private fun updatePieChart(expenseMap: Map<String, Float>, totalAmount: Float) {
        val pieEntries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        val categoryPercentages = mutableMapOf<String, Float>()

        val predefinedColors = listOf(
            Color.rgb(255, 99, 132), Color.rgb(54, 162, 235), Color.rgb(255, 206, 86),
            Color.rgb(75, 192, 192), Color.rgb(153, 102, 255), Color.rgb(255, 159, 64),
            Color.rgb(233, 30, 99), Color.rgb(66, 133, 244), Color.rgb(244, 180, 0),
            Color.rgb(15, 157, 88), Color.rgb(255, 87, 34), Color.rgb(0, 150, 136),
            Color.rgb(121, 85, 72), Color.rgb(63, 81, 181), Color.rgb(33, 150, 243)
        )

        val categoryColors = mutableMapOf<String, Int>()
        var colorIndex = 0

        for ((category, amount) in expenseMap) {
            val percentage = (amount / totalAmount) * 100
            categoryPercentages[category] = percentage  // Yüzdeyi kaydediyoruz

            // PieEntry ekliyoruz
            pieEntries.add(PieEntry(percentage, category))

            // Kategorilere renk atıyoruz
            if (!categoryColors.containsKey(category)) {
                categoryColors[category] = predefinedColors[colorIndex % predefinedColors.size]
                colorIndex++
            }
            colors.add(categoryColors[category]!!)
        }

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.colors = colors
        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.valueTextSize = 18f
        pieDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${String.format("%.1f", value)}%"
            }
        }

        pieDataSet.setDrawValues(false)

        val pieData = PieData(pieDataSet)
        binding.pieChart.data = pieData
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setDrawEntryLabels(false)
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()

        val legend = binding.pieChart.legend
        legend.textSize = 16f // Yazı boyutu
        legend.formSize = 16f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.isWordWrapEnabled = true
        legend.xEntrySpace = 20f
        legend.yEntrySpace = 20f
        binding.pieChart.legend.isEnabled = false

        createCustomLegend(expenseMap, categoryColors, categoryPercentages)

    }

    //Income starts
    private fun fetchIncomeFromFirestore() {
        if (userId.isEmpty()) return

        firestore.collection("users")
            .document(userId)
            .collection("incomes")
            .addSnapshotListener { result, exception ->
                if (exception != null) {
                    println("Gelir verisi alınamadı: ${exception.message}")
                    return@addSnapshotListener
                }

                val incomeMap = mutableMapOf<String, Float>()
                var totalAmount = 0f

                result?.forEach { document ->
                    val category = document.getString("category") ?: "Diğer"
                    val amount = document.getDouble("amount")?.toFloat() ?: 0f
                    incomeMap[category] = incomeMap.getOrDefault(category, 0f) + amount
                    totalAmount += amount
                }

                updatePieChartForIncome(incomeMap, totalAmount)
            }
    }

    private fun createCustomLegendForIncome(
        incomeMap: Map<String, Float>,
        categoryColors: Map<String, Int>,
        categoryPercentages: Map<String, Float>
    ) {
        binding.customLegendLayoutForIncome.removeAllViews()
        val inflater = LayoutInflater.from(context)

        for ((category, _) in incomeMap) {
            val legendItem =
                inflater.inflate(R.layout.item_legend, binding.customLegendLayoutForIncome, false)
            val colorBox = legendItem.findViewById<View>(R.id.colorBox)
            val label = legendItem.findViewById<android.widget.TextView>(R.id.label)
            val percent = legendItem.findViewById<android.widget.TextView>(R.id.percent)

            colorBox.setBackgroundColor(categoryColors[category] ?: Color.GRAY)
            label.text = category

            // Yüzdeyi TextView'e yaz
            percent.text = String.format("%.1f%%", categoryPercentages[category] ?: 0f)

            binding.customLegendLayoutForIncome.addView(legendItem)
        }
    }

    private fun updatePieChartForIncome(incomeMap: Map<String, Float>, totalAmount: Float) {
        val pieEntries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        val categoryPercentages = mutableMapOf<String, Float>()

        val predefinedColors = listOf(
            Color.rgb(255, 99, 132), Color.rgb(54, 162, 235), Color.rgb(255, 206, 86),
            Color.rgb(75, 192, 192), Color.rgb(153, 102, 255), Color.rgb(255, 159, 64),
            Color.rgb(233, 30, 99), Color.rgb(66, 133, 244), Color.rgb(244, 180, 0),
            Color.rgb(15, 157, 88), Color.rgb(255, 87, 34), Color.rgb(0, 150, 136),
            Color.rgb(121, 85, 72), Color.rgb(63, 81, 181), Color.rgb(33, 150, 243)
        )

        val categoryColors = mutableMapOf<String, Int>()
        var colorIndex = 0

        for ((category, amount) in incomeMap) {
            val percentage = (amount / totalAmount) * 100
            categoryPercentages[category] = percentage  // Yüzdeyi kaydediyoruz

            // PieEntry ekliyoruz
            pieEntries.add(PieEntry(percentage, category))

            // Kategorilere renk atıyoruz
            if (!categoryColors.containsKey(category)) {
                categoryColors[category] = predefinedColors[colorIndex % predefinedColors.size]
                colorIndex++
            }
            colors.add(categoryColors[category]!!)
        }

        val pieDataSet1 = PieDataSet(pieEntries, "")
        pieDataSet1.colors = colors
        pieDataSet1.valueTextColor = Color.WHITE
        pieDataSet1.valueTextSize = 18f
        pieDataSet1.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${String.format("%.1f", value)}%"
            }
        }

        pieDataSet1.setDrawValues(false)

        val pieData = PieData(pieDataSet1)
        binding.pieChart1.data = pieData
        binding.pieChart1.description.isEnabled = false
        binding.pieChart1.setDrawEntryLabels(false)
        binding.pieChart1.setUsePercentValues(true)
        binding.pieChart1.animateY(1000)
        binding.pieChart1.invalidate()

        val legend = binding.pieChart1.legend
        legend.textSize = 16f // Yazı boyutu
        legend.formSize = 16f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.isWordWrapEnabled = true
        legend.xEntrySpace = 20f
        legend.yEntrySpace = 20f
        binding.pieChart1.legend.isEnabled = false

        createCustomLegendForIncome(incomeMap, categoryColors, categoryPercentages)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
