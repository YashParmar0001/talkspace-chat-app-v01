package com.example.talkspace.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.R

class StatusAdapter (private val context: Context): RecyclerView.Adapter<StatusAdapter.StatusViewHolder> (){
    class StatusViewHolder(private val view: View):RecyclerView.ViewHolder(view){
        val imageView  = view.findViewById<ImageView>(R.id.status_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val adapter = LayoutInflater.from(parent.context)
            .inflate(R.layout.status_item,parent,false)
        return StatusViewHolder(adapter)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 50
    }
}