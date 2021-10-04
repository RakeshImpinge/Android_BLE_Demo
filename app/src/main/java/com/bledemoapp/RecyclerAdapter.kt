package com.bleadvertiseapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bledemoapp.R

class RecyclerAdapter(val mList: List<String>) :
    RecyclerView.Adapter<RecyclerAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.txt_uuid.text = "Device name is -- " + mList.get(position)
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt_uuid: TextView = itemView.findViewById(R.id.txt_uuid)
    }
}