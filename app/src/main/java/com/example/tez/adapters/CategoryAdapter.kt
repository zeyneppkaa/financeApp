package com.example.tez.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.tez.R
import com.example.tez.model.Category

class CategoryAdapter(private val categories: List<Category>, private val onItemClick: (Category) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedCategory: Category? = null

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_category)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val cardView: CardView = itemView.findViewById(R.id.card_view)

        fun bind(category: Category) {
            textView.text = category.name
            imageView.setImageResource(category.iconRes)

            // Seçilen kategorinin görünümünü "aktif" hale getirme
            if (selectedCategory == category) {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.expense_input_bg)) // Aktif kategori için arka plan rengi
                textView.setTextColor(itemView.context.getColor(R.color.selected_category_text)) // Text rengi
            } else {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.default_category))  // Diğerleri için varsayılan arka plan
                textView.setTextColor(itemView.context.getColor(R.color.default_category_text)) // Diğerleri için text rengi
            }

            itemView.setOnClickListener {
                selectedCategory = category
                onItemClick(category)
                notifyDataSetChanged()  // Seçilen öğeyi güncelle
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    fun getSelectedCategory(): Category? = selectedCategory

    fun clearSelectedCategory() {
        selectedCategory = null
        notifyDataSetChanged()  // RecyclerView'u yenile
    }
}
