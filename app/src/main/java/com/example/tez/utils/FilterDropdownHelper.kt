package com.example.tez.utils

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.example.tez.R

class FilterDropdownHelper(
    private val context: Context,
    private val anchorView: View,
    private val filterOptions: List<String>,
    private val onFilterSelected: (String) -> Unit
) {
    fun showFilterMenu() {
        val popupMenu = PopupMenu(context, anchorView)
        filterOptions.forEachIndexed { index, filter ->
            popupMenu.menu.add(0, index, 0, filter)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            onFilterSelected(filterOptions[menuItem.itemId])
            true
        }

        popupMenu.show()
    }
}
