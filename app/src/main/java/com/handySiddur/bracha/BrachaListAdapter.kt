package com.handySiddur.bracha

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView


class BrachaListAdapter(
    val items: ArrayList<BrachaItem>,
    val context: Context,
    val listener: ItemSelectedListener
) : RecyclerView.Adapter<BrachaListAdapter.BrachaViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrachaViewHolder {

        return BrachaViewHolder(LayoutInflater.from(context).inflate(R.layout.bracha_item_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BrachaViewHolder, position: Int) {
        holder.menuIcon.setImageResource(items.get(position).image)
        holder.menuItem.setText(items.get(position).name)
        holder.menuItem.typeface = ResourcesCompat.getFont(context, R.font.open_sans)
        holder.menuItem.setOnClickListener {listener.selected(position)}
    }

    class BrachaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var menuContainer: RelativeLayout
        internal var menuItem: TextView
        internal var menuIcon: ImageView

        init {
            menuContainer = view.findViewById(R.id.menu_container)
            menuItem = view.findViewById(R.id.menu_item)
            menuIcon = view.findViewById(R.id.menu_icon)
        }
    }

}