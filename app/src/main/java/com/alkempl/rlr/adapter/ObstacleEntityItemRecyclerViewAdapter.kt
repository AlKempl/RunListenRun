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
import android.graphics.Color
import android.graphics.Typeface
import com.alkempl.rlr.data.LocationRepository
import com.alkempl.rlr.data.ObstacleRepository
import com.alkempl.rlr.data.db.ObstacleEntity
import com.alkempl.rlr.data.model.obstacle.ObstacleStatus
import java.util.concurrent.Executors


class ObstacleEntityItemRecyclerViewAdapter(
    private var context: Context,
    private var values: List<ObstacleEntity>
) : RecyclerView.Adapter<ObstacleEntityItemRecyclerViewAdapter.ViewHolder>()  {

    private val obstacleRepository = ObstacleRepository.getInstance(
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
        when(item.status){
            ObstacleStatus.SUCCEEDED -> {
                holder.contentView.setTextColor(Color.parseColor("#ff008000"))
                holder.contentView.typeface = Typeface.DEFAULT_BOLD
            }

            ObstacleStatus.FAILED -> {
                holder.contentView.setTextColor(Color.parseColor("#ff800000"))
                holder.contentView.typeface = Typeface.DEFAULT_BOLD
            }

            else -> {}
        }
    }

    override fun getItemCount(): Int = values.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateUserList(obstacles:  List<ObstacleEntity>) {
        this.values = obstacles
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
            /*// Get the position of the item that was clicked.
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
            notifyDataSetChanged()*/
        }
    }
}