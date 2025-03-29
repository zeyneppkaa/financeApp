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

enum class SelectionMode {
    SINGLE, MULTIPLE
}
class CategoryAdapter(
    private val categories: List<Category>,
    private val selectionMode: SelectionMode,
    private val onItemClick: (List<Category>) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val selectedCategories = mutableSetOf<Category>()

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_category)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val cardView: CardView = itemView.findViewById(R.id.card_view)

        fun bind(category: Category) {
            textView.text = category.name
            imageView.setImageResource(category.iconRes)

            val isSelected = selectedCategories.contains(category)

            if (isSelected) {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.selected_category_bg))
                textView.setTextColor(itemView.context.getColor(R.color.selected_category_text))
            } else {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.default_category_bg))
                textView.setTextColor(itemView.context.getColor(R.color.default_category_text))
            }

            itemView.setOnClickListener {
                if (selectedCategories.contains(category)) {
                    selectedCategories.remove(category)
                } else {
                    selectedCategories.add(category)
                }
                onItemClick(selectedCategories.toList())
                notifyDataSetChanged()
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

    fun setSelectedCategories(selected: Set<Category>) {
        selectedCategories.clear()
        selectedCategories.addAll(selected)
        notifyDataSetChanged()
    }
}

