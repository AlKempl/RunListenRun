package com.alkempl.rlr.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.alkempl.rlr.data.db.LocationEntity

import com.alkempl.rlr.databinding.FragmentItemBinding

/**
 * [RecyclerView.Adapter] that can display a [LocationEntity].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(
    values: List<LocationEntity>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {
    private var values : List<LocationEntity>

    init {
        this.values = values
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.getPrefix()
        holder.contentView.text = item.toString()
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateUserList(locations:  ArrayList<LocationEntity>) {
        this.values = locations
        notifyDataSetChanged()
    }

}