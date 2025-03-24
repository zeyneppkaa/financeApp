package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.adapters.CategoryAdapter
import com.example.tez.databinding.FragmentAddBillBinding
import com.example.tez.model.Category

class AddBillFragment : Fragment() {

    private var _binding: FragmentAddBillBinding? = null
    private val binding get() = _binding!!

    private lateinit var billCategoryAdapter: CategoryAdapter
    private lateinit var billCategoryList: List<Category>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBillBinding.inflate(inflater, container, false)


        val recyclerView: RecyclerView = binding.rvBillCategories

        billCategoryList = listOf(
            Category("Electricity",R.drawable.ic_electricity),
            Category("Water",R.drawable.ic_water),
            Category("Internet",R.drawable.ic_internet),
            Category("Natural Gas",R.drawable.ic_natural_gas),
            Category("Mobile Bill",R.drawable.ic_mobile_bill),
            Category("Netflix",R.drawable.ic_netflix),
            Category("Spotify",R.drawable.ic_spotify),
            Category("Disney Plus",R.drawable.ic_disney_plus),
            Category("Youtube Music",R.drawable.ic_youtube_music),
            Category("Blu TV",R.drawable.ic_blutv)

            )

        billCategoryAdapter = CategoryAdapter(billCategoryList){ selectedCategory ->
            if (selectedCategory != null) {
                Toast.makeText(requireContext(), "Seçilen Kategori: ${selectedCategory.name}", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = billCategoryAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}