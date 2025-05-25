package com.example.tez

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tez.databinding.FragmentAnalysisBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AnalysisFragment : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val filterOptions = listOf("See All", "Last 7 days", "This month", "Last 1 month", "Last 3 months")
    private var selectedFilter = "See All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvFilter.text = selectedFilter
        binding.tvFilter.setOnClickListener {
            showFilterDialog()
        }

        fetchExpensesFromFirestoreWithFilter(selectedFilter)
        fetchIncomeFromFirestoreWithFilter(selectedFilter)
    }

    private fun showFilterDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Filter")

        builder.setItems(filterOptions.toTypedArray()) { _, which ->
            selectedFilter = filterOptions[which]
            binding.tvFilter.text = selectedFilter
            fetchExpensesFromFirestoreWithFilter(selectedFilter)
            fetchIncomeFromFirestoreWithFilter(selectedFilter)
        }

        builder.create().show()
    }

    // Tarih filtreleri için yardımcı fonksiyonlar
    private fun getPastDate(days: Int): Timestamp {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -days)
        return Timestamp(calendar.time)
    }

    private fun getMonthStart(): Timestamp {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        return Timestamp(calendar.time)
    }

    private fun fetchExpensesFromFirestoreWithFilter(filter: String) {
        if (userId.isEmpty()) return

        val expensesRef = firestore.collection("users")
            .document(userId)
            .collection("expenses")

        val query: Query = when (filter) {
            "Last 7 days" -> expensesRef.whereGreaterThan("date", getPastDate(7))
            "This month" -> expensesRef.whereGreaterThan("date", getMonthStart())
            "Last 1 month" -> expensesRef.whereGreaterThan("date", getPastDate(30))
            "Last 3 months" -> expensesRef.whereGreaterThan("date", getPastDate(90))
            else -> expensesRef
        }.orderBy("date", Query.Direction.DESCENDING)

        query.get()
            .addOnSuccessListener { result ->
                val expenseMap = mutableMapOf<String, Float>()
                var totalAmount = 0f

                for (document in result) {
                    val category = document.getString("category") ?: "Diğer"
                    val amount = document.getDouble("amount")?.toFloat() ?: 0f
                    expenseMap[category] = expenseMap.getOrDefault(category, 0f) + amount
                    totalAmount += amount
                }

                // Bills toplamını da ekleyelim (filtreyi bills için eklemedim, istersen uyarlayabiliriz)
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
                    .addOnFailureListener {
                        updatePieChart(expenseMap, totalAmount)
                    }
            }
            .addOnFailureListener {
                println("Gider verisi alınamadı: ${it.message}")
            }
    }

    private fun fetchIncomeFromFirestoreWithFilter(filter: String) {
        if (userId.isEmpty()) return

        val incomesRef = firestore.collection("users")
            .document(userId)
            .collection("incomes")

        val query: Query = when (filter) {
            "Last 7 days" -> incomesRef.whereGreaterThan("date", getPastDate(7))
            "This month" -> incomesRef.whereGreaterThan("date", getMonthStart())
            "Last 1 month" -> incomesRef.whereGreaterThan("date", getPastDate(30))
            "Last 3 months" -> incomesRef.whereGreaterThan("date", getPastDate(90))
            else -> incomesRef
        }.orderBy("date", Query.Direction.DESCENDING)

        query.get()
            .addOnSuccessListener { result ->
                val incomeMap = mutableMapOf<String, Float>()
                var totalAmount = 0f

                for (document in result) {
                    val category = document.getString("category") ?: "Diğer"
                    val amount = document.getDouble("amount")?.toFloat() ?: 0f
                    incomeMap[category] = incomeMap.getOrDefault(category, 0f) + amount
                    totalAmount += amount
                }

                updatePieChartForIncome(incomeMap, totalAmount)
            }
            .addOnFailureListener {
                println("Gelir verisi alınamadı: ${it.message}")
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
            if (totalAmount == 0f) continue
            val percentage = (amount / totalAmount) * 100
            categoryPercentages[category] = percentage

            pieEntries.add(PieEntry(percentage, category))

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
        legend.textSize = 16f
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

    private fun createCustomLegend(
        expenseMap: Map<String, Float>,
        categoryColors: Map<String, Int>,
        categoryPercentages: Map<String, Float>
    ) {
        binding.customLegendLayout.removeAllViews()
        val inflater = LayoutInflater.from(context)

        for ((category, _) in expenseMap) {
            val legendItem = inflater.inflate(R.layout.item_legend, binding.customLegendLayout, false)

            val colorBox = legendItem.findViewById<View>(R.id.colorBox)
            val label = legendItem.findViewById<TextView>(R.id.label)
            val percent = legendItem.findViewById<TextView>(R.id.percent)

            colorBox.setBackgroundColor(categoryColors[category] ?: Color.GRAY)
            label.text = category
            percent.text = String.format("%.1f%%", categoryPercentages[category] ?: 0f)

            binding.customLegendLayout.addView(legendItem)
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
            if (totalAmount == 0f) continue
            val percentage = (amount / totalAmount) * 100
            categoryPercentages[category] = percentage

            pieEntries.add(PieEntry(percentage, category))

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
        binding.pieChart1.data = pieData
        binding.pieChart1.description.isEnabled = false
        binding.pieChart1.setDrawEntryLabels(false)
        binding.pieChart1.setUsePercentValues(true)
        binding.pieChart1.animateY(1000)
        binding.pieChart1.invalidate()

        val legend = binding.pieChart1.legend
        legend.textSize = 16f
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

    private fun createCustomLegendForIncome(
        incomeMap: Map<String, Float>,
        categoryColors: Map<String, Int>,
        categoryPercentages: Map<String, Float>
    ) {
        binding.customLegendLayoutForIncome.removeAllViews()
        val inflater = LayoutInflater.from(context)

        for ((category, _) in incomeMap) {
            val legendItem = inflater.inflate(R.layout.item_legend, binding.customLegendLayoutForIncome, false)

            val colorBox = legendItem.findViewById<View>(R.id.colorBox)
            val label = legendItem.findViewById<TextView>(R.id.label)
            val percent = legendItem.findViewById<TextView>(R.id.percent)

            colorBox.setBackgroundColor(categoryColors[category] ?: Color.GRAY)
            label.text = category
            percent.text = String.format("%.1f%%", categoryPercentages[category] ?: 0f)

            binding.customLegendLayoutForIncome.addView(legendItem)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
