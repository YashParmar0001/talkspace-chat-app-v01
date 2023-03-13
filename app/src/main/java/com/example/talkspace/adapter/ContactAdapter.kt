package com.example.talkspace.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.R
import com.example.talkspace.databinding.AddContactViewBinding
import com.example.talkspace.databinding.ContactViewBinding
import com.example.talkspace.databinding.CreateContactViewBinding
import com.example.talkspace.databinding.CreateGroupViewBinding
import com.example.talkspace.model.SQLiteContact

class ContactAdapter(val context: Context) :
    ListAdapter<SQLiteContact, RecyclerView.ViewHolder>(ContactComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.contact_view, parent, false)
        val binding = ContactViewBinding.bind(view)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemView.setOnClickListener {
            Log.d("Contacts", "Contact clicked: ${item.contactName}")
        }

        val demiContact = SQLiteContact("", "", "", "", false)

        when (holder.itemViewType) {
            0 -> {
                demiContact.contactName = CREATE_NEW_GROUP
                (holder as ContactViewHolder).bind(demiContact)
                holder.itemView.setOnClickListener {
                    Log.d("Contacts", "Creating new group")
                }
            }

            1 -> {
                demiContact.contactName = CREATE_NEW_CONTACT
                (holder as ContactViewHolder).bind(demiContact)
                holder.itemView.setOnClickListener {
                    Log.d("Contacts", "Creating new contact")
                }
            }

            2 -> {
                demiContact.contactName = ADD_CONTACT
                (holder as ContactViewHolder).bind(demiContact)
                holder.itemView.setOnClickListener {
                    Log.d("Contacts", "Adding new contact")
                }
            }

            else -> {
                (holder as ContactViewHolder).bind(item)
                holder.itemView.setOnClickListener {
                    Log.d("Contacts", "Contact clicked: ${item.contactName}")
                }
            }
        }
    }

    inner class ContactViewHolder(private val binding: ContactViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SQLiteContact) {
            if (item.contactPhoneNumber == "") {
                binding.contactName.text = item.contactName
                binding.contactAbout.text = ""
                when (item.contactName) {
                    CREATE_NEW_GROUP -> {
                        binding.contactPhoto.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.ic_baseline_group_add_24)
                        )
                    }
                    CREATE_NEW_CONTACT -> {
                        binding.contactPhoto.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.baseline_person_add_24)
                        )
                    }
                    else -> {
                        binding.contactPhoto.setImageDrawable(
                            ContextCompat.getDrawable(context, R.drawable.ic_baseline_person_24)
                        )
                    }
                }
            }else {
                binding.contactName.text = item.contactName
            }
        }
    }

    class CreateGroupViewHolder(binding: CreateGroupViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }

    class CreateContactViewHolder(binding: CreateContactViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }

    class AddContactViewHolder(binding: AddContactViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {}
    }

    override fun getItemViewType(position: Int): Int {
        return when (position < 3) {
            true -> position
            else -> 3
        }
    }

    class ContactComparator : DiffUtil.ItemCallback<SQLiteContact>() {
        override fun areItemsTheSame(oldItem: SQLiteContact, newItem: SQLiteContact): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SQLiteContact, newItem: SQLiteContact): Boolean {
            return oldItem.contactPhoneNumber == newItem.contactPhoneNumber
        }

    }

    companion object {
        const val CREATE_NEW_GROUP = "Create new group"
        const val CREATE_NEW_CONTACT = "Create new contact"
        const val ADD_CONTACT = "Add contact"
    }

}