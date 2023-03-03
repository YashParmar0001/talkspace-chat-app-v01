package com.example.talkspace.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.R
import com.example.talkspace.databinding.ChatViewBinding
import com.example.talkspace.model.SQLChat
import com.example.talkspace.viewmodels.ChatViewModel

class ChatListAdapter(private val chatViewModel: ChatViewModel) :
    ListAdapter<SQLChat, ChatListAdapter.ChatViewHolder>(ChatsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat_view, parent, false)
        val binding = ChatViewBinding.bind(view)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        holder.itemView.setOnClickListener {
            Log.d("ChatListFragment", "Chat clicked")
            chatViewModel.setCurrentFriendId(item.phoneNumber)
            chatViewModel.setCurrentFriendName(item.friendName)
            Log.d("Chats", "Name: ${chatViewModel.currentFriendName.value}")
            Log.d("Chats", "Id: ${chatViewModel.currentFriendId.value}")
//            holder.itemView.findNavController().navigate(R.id.action_mainFragment_to_chatFragment)
        }
    }

    class ChatViewHolder(private val binding: ChatViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SQLChat) {
            binding.friendName.text = item.friendName
        }
    }

    class ChatsComparator : DiffUtil.ItemCallback<SQLChat>() {
        override fun areItemsTheSame(oldItem: SQLChat, newItem: SQLChat): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SQLChat, newItem: SQLChat): Boolean {
            return oldItem.phoneNumber == newItem.phoneNumber
        }
    }

}