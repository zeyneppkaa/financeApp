package com.example.tez

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.tez.databinding.FragmentHomeBinding
import com.example.tez.model.Expense
import com.example.tez.utils.RoundedBarChartRenderer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var currentFilter = "Daily"
    private var currentWeekOffset = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Kullanıcı ismini çek ve göster
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    binding.tvUserName.text = "Hello, $name"
                } else {
                    binding.tvUserName.text = "Hello, User"
                }
            }
            .addOnFailureListener {
                binding.tvUserName.text = "Hello, User"
            }

        fetchTotalBalance()
        fetchTotalIncomeThisMonth()
        fetchTotalExpenseThisMonth()

        setupFilterButtons()
        binding.btnDaily.performClick()

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

        // Sol/sağ ok dinleyicileri
        binding.btnOverviewLeftArrow.setOnClickListener {
            if (currentFilter == "Daily") {
                currentWeekOffset--
                fetchExpensesAndDisplay(currentFilter)
            }
        }

        binding.btnOverviewRightArrow.setOnClickListener {
            if (currentFilter == "Daily") {
                currentWeekOffset++
                fetchExpensesAndDisplay(currentFilter)
            }
        }
    }

    private fun checkFirstTimeBillsClick() {
        val userId = auth.currentUser?.uid ?: return
        val sharedPref = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val hasClickedBills = sharedPref.getBoolean("hasClickedBills_$userId", false)

        if (!hasClickedBills) {
            val action = HomeFragmentDirections.actionHomeFragmentToTemporaryBillsFragment()
            findNavController().navigate(action)
            sharedPref.edit().putBoolean("hasClickedBills_$userId", true).apply()
        } else {
            val action = HomeFragmentDirections.actionHomeFragmentToBillsFragment()
            findNavController().navigate(action)
        }
    }

    private fun fetchTotalBalance() {
        if (userId.isEmpty()) return

        val incomesRef = firestore.collection("users").document(userId).collection("incomes")
        val expensesRef = firestore.collection("users").document(userId).collection("expenses")

        incomesRef.get().addOnSuccessListener { incomeResult ->
            var totalIncome = 0.0
            for (doc in incomeResult) {
                totalIncome += doc.getDouble("amount") ?: 0.0
            }

            expensesRef.get().addOnSuccessListener { expenseResult ->
                var totalExpense = 0.0
                for (doc in expenseResult) {
                    totalExpense += doc.getDouble("amount") ?: 0.0
                }

                val totalBalance = totalIncome - totalExpense

                val formattedBalance = String.format("₺ %, .2f", kotlin.math.abs(totalBalance))

                // TextView'de yazıyı ve rengi güncelle
                binding.tvTotalBalance.apply {
                    text = if (totalBalance < 0) "-$formattedBalance" else formattedBalance
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (totalBalance < 0) R.color.red else R.color.white
                        )
                    )
                }
            }
        }
    }

    private fun fetchTotalIncomeThisMonth() {
        if (userId.isEmpty()) return

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.time

        firestore.collection("users").document(userId).collection("incomes")
            .whereGreaterThanOrEqualTo("date", startOfMonth)
            .get()
            .addOnSuccessListener { result ->
                var total = 0.0
                for (doc in result) {
                    total += doc.getDouble("amount") ?: 0.0
                }
                binding.tvTotalIncomeAmount.text = "+ ₺ ${String.format("%,.2f", total)}"
            }
    }

    private fun fetchTotalExpenseThisMonth() {
        if (userId.isEmpty()) return

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.time

        firestore.collection("users").document(userId).collection("expenses")
            .whereGreaterThanOrEqualTo("date", startOfMonth)
            .get()
            .addOnSuccessListener { result ->
                var total = 0.0
                for (doc in result) {
                    total += doc.getDouble("amount") ?: 0.0
                }
                binding.tvTotalExpenseAmount.text = "- ₺ ${String.format("%,.2f", total)}"
            }
    }

    private fun setupFilterButtons() {
        val buttons = listOf(binding.btnDaily, binding.btnWeekly, binding.btnMonthly)

        for (button in buttons) {
            button.setOnClickListener {
                buttons.forEach {
                    it.isSelected = false
                    it.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                }

                button.isSelected = true
                button.setBackgroundColor(Color.parseColor("#89D7F5"))

                currentFilter = when (button.id) {
                    R.id.btnDaily -> "Daily"
                    R.id.btnWeekly -> "Weekly"
                    R.id.btnMonthly -> "Monthly"
                    else -> "Daily"
                }

                if (currentFilter != "Daily") currentWeekOffset = 0

                fetchExpensesAndDisplay(currentFilter)
            }
        }
    }

    private fun fetchExpensesAndDisplay(timeframe: String) {
        if (userId.isEmpty()) return

        val calendar = Calendar.getInstance()

        val (startDate, labelFormat, groupCount, labelGenerator) = when (timeframe) {
            "Daily" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -6 + currentWeekOffset * 7)
                Quadruple(calendar.time, "EEE", 7) { cal: Calendar ->
                    cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)!!
                }
            }

            "Weekly" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                Quadruple(calendar.time, "w", 4) { cal: Calendar ->
                    val week = cal.get(Calendar.WEEK_OF_MONTH)
                    "${week}st Week"
                }
            }

            "Monthly" -> {
                calendar.add(Calendar.MONTH, -5)
                Quadruple(calendar.time, "MMM", 6) { cal: Calendar ->
                    cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)!!
                }
            }

            else -> return
        }

        firestore.collection("users").document(userId).collection("expenses")
            .whereGreaterThanOrEqualTo("date", startDate)
            .get()
            .addOnSuccessListener { result ->
                val groupMap = mutableMapOf<String, Float>()

                result.forEach { doc ->
                    val expense = doc.toObject(Expense::class.java)
                    val date = expense.date.toDate()
                    val cal = Calendar.getInstance()
                    cal.time = date

                    when (timeframe) {
                        "Weekly" -> {
                            val day = cal.get(Calendar.DAY_OF_MONTH)
                            val weekIndex = when {
                                day <= 7 -> 0
                                day <= 14 -> 1
                                day <= 21 -> 2
                                else -> 3
                            }
                            val key = "${weekIndex + 1}st Week"
                            groupMap[key] = (groupMap[key] ?: 0f) + expense.amount.toFloat()
                        }
                        "Daily" -> {
                            val sdf = SimpleDateFormat("dd MMM", Locale.ENGLISH)
                            val key = sdf.format(date)
                            groupMap[key] = (groupMap[key] ?: 0f) + expense.amount.toFloat()
                        }

                        else -> {
                            val sdf = SimpleDateFormat(labelFormat, Locale.ENGLISH)
                            val key = sdf.format(date)
                            groupMap[key] = (groupMap[key] ?: 0f) + expense.amount.toFloat()
                        }
                    }
                }

                val labels = ArrayList<String>()
                val entries = ArrayList<BarEntry>()

                if (timeframe == "Daily") {
                    val sdfRange = SimpleDateFormat("dd MMM", Locale.ENGLISH)
                    val startCal = Calendar.getInstance()
                    startCal.add(Calendar.DAY_OF_YEAR, -6 + currentWeekOffset * 7)
                    val endCal = Calendar.getInstance()
                    endCal.add(Calendar.DAY_OF_YEAR, currentWeekOffset * 7)
                    val rangeText = "${sdfRange.format(startCal.time)} - ${sdfRange.format(endCal.time)}"
                    binding.btnOverviewLeftArrow.visibility = View.VISIBLE
                    binding.btnOverviewRightArrow.visibility = View.VISIBLE
                    binding.tvWeekRange.text = rangeText
                    binding.tvWeekRange.visibility = View.VISIBLE
                } else {
                    binding.tvWeekRange.visibility = View.GONE
                    binding.btnOverviewLeftArrow.visibility = View.GONE
                    binding.btnOverviewRightArrow.visibility = View.GONE
                }

                when (timeframe) {
                    "Weekly" -> {
                        repeat(4) { index ->
                            val label = "${index + 1}st Week"
                            labels.add(label)
                            entries.add(BarEntry(index.toFloat(), groupMap[label] ?: 0f))
                        }
                    }

                    "Daily" -> {
                        calendar.time = startDate
                        repeat(groupCount) {
                            val currentCal = calendar.clone() as Calendar
                            val key = SimpleDateFormat("dd MMM", Locale.ENGLISH).format(currentCal.time)

                            // X ekseni için sadece gün adı ("Mon", "Tue" vs)
                            val dayLabel = currentCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH) ?: ""
                            labels.add(dayLabel)

                            entries.add(BarEntry(it.toFloat(), groupMap[key] ?: 0f))

                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }

                    "Monthly" -> {
                        calendar.time = startDate
                        repeat(groupCount) {
                            val currentCal = calendar.clone() as Calendar
                            val key = SimpleDateFormat("MMM", Locale.ENGLISH).format(currentCal.time)

                            labels.add(currentCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH) ?: "")
                            entries.add(BarEntry(it.toFloat(), groupMap[key] ?: 0f))

                            calendar.add(Calendar.MONTH, 1)
                        }
                    }
                }

                updateChart(entries, labels)
            }
    }

    private fun updateChart(entries: List<BarEntry>, labels: List<String>) {
        val barDataSet = BarDataSet(entries, "")
        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.red)
        barDataSet.valueTextSize = 12f
        barDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        barDataSet.setDrawValues(true)

        val data = BarData(barDataSet)
        data.barWidth = 0.2f

        val barChart = binding.barChart
        barChart.renderer = RoundedBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler)
        barChart.data = data
        barChart.setFitBars(true)
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.animateY(500)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.black)
        xAxis.axisLineWidth = 2f
        xAxis.granularity = 1f
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)
        xAxis.textSize = 12f
        xAxis.setAvoidFirstLastClipping(false)
        xAxis.labelCount = labels.size
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = labels.size - 0.5f

        barChart.axisRight.isEnabled = false

        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.enableGridDashedLine(10f, 10f, 0f)
        leftAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.overview_chart_left_axis)
        leftAxis.setDrawLabels(true)
        leftAxis.setDrawAxisLine(false)
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.overview_chart_left_axis)
        leftAxis.textSize = 12f
        leftAxis.setLabelCount(5, true)
        leftAxis.axisMinimum = 0f
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when {
                    value >= 1_000_000 -> "₺${(value / 1_000_000).toInt()}M"
                    value >= 1_000 -> "₺${(value / 1_000).toInt()}K"
                    else -> "₺${value.toInt()}"
                }
            }
        }

        barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class Quadruple<T1, T2, T3, T4>(
        val first: T1,
        val second: T2,
        val third: T3,
        val fourth: T4
    )
}
