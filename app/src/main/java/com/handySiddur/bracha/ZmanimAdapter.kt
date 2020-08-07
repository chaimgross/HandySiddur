package com.handySiddur.bracha;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

class ZmanimAdapter(
    val items: ArrayList<Pair<String, String>>,
    val context: Context) : RecyclerView.Adapter<ZmanimAdapter.ZmanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZmanViewHolder {

        return ZmanViewHolder(LayoutInflater.from(context).inflate(R.layout.zman_item_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ZmanViewHolder, position: Int) {
        holder.zmanTitle.text = items.get(position).first
        holder.zmanValue.text = items.get(position).second
    }

    class ZmanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var zmanTitle: TextView
        internal var zmanValue: TextView

        init {
            zmanTitle = view.findViewById(R.id.zman_title)
            zmanValue = view.findViewById(R.id.zman_value)
            zmanTitle.typeface = ResourcesCompat.getFont(view.context, R.font.open_sans);
            zmanValue.typeface = ResourcesCompat.getFont(view.context, R.font.open_sans);
        }
    }

}