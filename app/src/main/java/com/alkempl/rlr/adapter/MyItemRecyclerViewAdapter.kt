package com.alkempl.rlr.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alkempl.rlr.data.db.LocationEntity

import com.alkempl.rlr.databinding.FragmentItemBinding
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.ImageView
import com.alkempl.rlr.R
import com.alkempl.rlr.data.LocationRepository
import java.util.concurrent.Executors


/**
 * [RecyclerView.Adapter] that can display a [LocationEntity].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(
    private var context: Context,
    private var values: List<LocationEntity>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>()  {

    private val locationRepository = LocationRepository.getInstance(
        context,
        Executors.newSingleThreadExecutor()
    )

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
        if(item.checked){
            holder.contentView.setTextColor(Color.parseColor("#ff008000"))
            holder.contentView.typeface = Typeface.DEFAULT_BOLD
        }
    }

    override fun getItemCount(): Int = values.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateUserList(locations:  List<LocationEntity>) {
        this.values = locations
        notifyDataSetChanged()
    }

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        init{
            binding.root.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?) {
            // Get the position of the item that was clicked.
            val mPosition = layoutPosition
            // Use that to access the affected item in mWordList.
            var element: LocationEntity = values[mPosition]
            element.longitude = 0.0
            element.latitude = 0.0
            element.checked = true
            Log.d("MIRVA_CLICK", element.toString())

            locationRepository.updateLocation(element)
//
//            // Change the word in the mWordList.
////            values.set(mPosition, "Clicked! ${element.toString()}")
//            // Notify the adapter, that the data has changed so it can
//            // update the RecyclerView to display the data.
            notifyDataSetChanged()
        }
    }
}