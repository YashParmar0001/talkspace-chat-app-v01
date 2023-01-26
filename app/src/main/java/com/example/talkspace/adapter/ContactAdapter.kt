package com.example.talkspace.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.R
import com.example.talkspace.databinding.ContactViewBinding
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.viewmodels.ChatViewModel

class ContactAdapter(private val chatViewModel: ChatViewModel) :
    ListAdapter<SQLiteContact, ContactAdapter.ContactViewHolder>(ContactComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_view, parent, false)
        val binding = ContactViewBinding.bind(view)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        holder.itemView.setOnClickListener {
            Log.d("Contacts", "Contact clicked: ${item.contactName}")
        }
    }

    class ContactViewHolder(private val binding: ContactViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SQLiteContact) {
            binding.contactName.text = item.contactName
        }
    }

    class ContactComparator : DiffUtil.ItemCallback<SQLiteContact>() {
        override fun areItemsTheSame(oldItem: SQLiteContact, newItem: SQLiteContact): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SQLiteContact, newItem: SQLiteContact): Boolean {
            return oldItem.contactId == newItem.contactId
        }

    }

}