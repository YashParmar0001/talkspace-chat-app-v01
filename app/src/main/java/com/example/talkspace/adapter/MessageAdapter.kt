package com.example.talkspace.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.R
import com.example.talkspace.databinding.ReceiverMessageViewBinding
import com.example.talkspace.databinding.SenderMessageViewBinding
import com.example.talkspace.model.SQLiteMessage
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter :
    ListAdapter<SQLiteMessage, RecyclerView.ViewHolder>(MessageComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        return when (viewType) {
            0 -> {
                view = inflater.inflate(R.layout.sender_message_view, parent, false)
                SendMessageViewHolder(SenderMessageViewBinding.bind(view))
            }
            else -> {
                view = inflater.inflate(R.layout.receiver_message_view, parent, false)
                ReceiveMessageViewHolder(ReceiverMessageViewBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder.itemViewType) {
            0 -> (holder as SendMessageViewHolder).bind(item)
            else -> (holder as ReceiveMessageViewHolder).bind(item)
        }
    }

    class SendMessageViewHolder(private val binding: SenderMessageViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SQLiteMessage) {
            binding.messageText.text = item.text
            binding.messageTime.text = item.timeStamp.toString()
        }
    }

    class ReceiveMessageViewHolder(private val binding: ReceiverMessageViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SQLiteMessage) {
            binding.messageText.text = item.text
            binding.messageTime.text = item.timeStamp.toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        // Check if message is sent by user
        return if (item.senderId == FirebaseAuth.getInstance().currentUser?.phoneNumber) {
            0
        } else {
            1
        }
    }

    class MessageComparator : DiffUtil.ItemCallback<SQLiteMessage>() {
        override fun areItemsTheSame(oldItem: SQLiteMessage, newItem: SQLiteMessage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SQLiteMessage, newItem: SQLiteMessage): Boolean {
            return oldItem.timeStamp == newItem.timeStamp
        }

    }
}