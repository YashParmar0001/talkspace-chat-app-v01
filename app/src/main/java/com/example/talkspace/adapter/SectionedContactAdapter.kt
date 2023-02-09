package com.example.talkspace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.talkspace.R
import com.example.talkspace.databinding.ContactViewBinding
import com.example.talkspace.databinding.ContactViewNoUserBinding
import com.example.talkspace.databinding.ContactViewUserBinding
import com.example.talkspace.databinding.SectionViewBinding
import com.example.talkspace.model.SQLiteContact

class SectionedContactAdapter(
    private val usersList: List<SQLiteContact>,
    private val nonUsersList: List<SQLiteContact>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        val binding: ViewBinding

        when (viewType) {
            USER_SECTION_TYPE -> {
                view = inflater.inflate(
                    R.layout.section_view, parent, false
                )
                binding = SectionViewBinding.bind(view)
                return SectionViewHolder(binding)
            }
            USER_TYPE -> {
                view = inflater.inflate(
                    R.layout.contact_view_user, parent, false
                )
                binding = ContactViewUserBinding.bind(view)
                return UserViewHolder(binding)
            }
            NON_USER_SECTION_TYPE -> {
                view = inflater.inflate(
                    R.layout.section_view, parent, false
                )
                binding = SectionViewBinding.bind(view)
                return SectionViewHolder(binding)
            }
            else -> {
                view = inflater.inflate(
                    R.layout.contact_view_no_user, parent, false
                )
                binding = ContactViewNoUserBinding.bind(view)
                return NonUserViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return usersList.size + nonUsersList.size + 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionViewHolder -> {
                if (position == 0) {
                    holder.bind("Users")
                }else {
                    holder.bind("Non users")
                }
            }
            is UserViewHolder -> {
                val item = usersList[position - 1]
                holder.bind(item)
            }
            is NonUserViewHolder -> {
                val item = nonUsersList[position - usersList.size - 2]
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> USER_SECTION_TYPE
            position <= usersList.size -> USER_TYPE
            position == usersList.size + 1 -> NON_USER_SECTION_TYPE
            else -> NON_USER_TYPE
        }
    }

    inner class SectionViewHolder(private val binding: SectionViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.sectionTitle.text = title
        }
    }

    inner class UserViewHolder(private val binding: ContactViewUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SQLiteContact) {
            binding.contactName.text = item.contactName
            binding.contactAbout.text = item.contactAbout
        }
    }

    inner class NonUserViewHolder(private val binding: ContactViewNoUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SQLiteContact) {
            binding.contactName.text = item.contactName
        }
    }

    companion object {
        const val NON_USER_TYPE = 0
        const val USER_TYPE = 1
        const val USER_SECTION_TYPE = 3
        const val NON_USER_SECTION_TYPE = 2
    }
}