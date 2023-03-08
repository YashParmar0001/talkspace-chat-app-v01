package com.example.talkspace.ui.chatsection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talkspace.ApplicationClass
import com.example.talkspace.R
import com.example.talkspace.adapter.MessageAdapter
import com.example.talkspace.databinding.FragmentChatBinding
import com.example.talkspace.model.*
import com.example.talkspace.observers.MyScrollToBottomObserver
import com.example.talkspace.ui.currentUser
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore


class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding

    private lateinit var messages: LiveData<List<SQLiteMessage>>
    private lateinit var currentFriendId: String
    private lateinit var currentFriendName: String

    private lateinit var adapter: MessageAdapter

    private val chatViewModel: ChatViewModel by activityViewModels {
        ChatViewModelFactory(
            (activity?.application as ApplicationClass).chatRepository,
            (activity?.application as ApplicationClass).contactRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        currentFriendId = chatViewModel.currentFriendId.value.toString()
        currentFriendName = chatViewModel.currentFriendName.value.toString()
        messages = chatViewModel.getMessages(currentFriendId)

        // For displaying messages
        val layoutManager =
            LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.messagesRecyclerview.fitsSystemWindows = true
//        layoutManager.stackFromEnd = true

        binding.messagesRecyclerview.layoutManager = layoutManager

        adapter = MessageAdapter()
        binding.messagesRecyclerview.adapter = adapter

        messages.observe(viewLifecycleOwner) { messages ->
            messages?.let {
                adapter.submitList(messages)
            }
        }

        adapter.registerAdapterDataObserver(
            MyScrollToBottomObserver(
                binding.messagesRecyclerview,
                adapter,
                layoutManager
            )
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backChatButton = getView()?.findViewById<ImageView>(R.id.back_btn)
        backChatButton?.setOnClickListener {
            findNavController().navigateUp()
        }

        // Set friend details
        val friendNameView = getView()?.findViewById<TextView>(R.id.friend_name)
        friendNameView?.text = chatViewModel.currentFriendName.value

        binding.sendMessageButton.setOnClickListener {
            Toast.makeText(requireContext(), "Sending message", Toast.LENGTH_LONG).show()
            sendMessage()
        }
    }

    override fun onStart() {
        super.onStart()
        chatViewModel.startListeningForMessages()
    }

    override fun onStop() {
        super.onStop()
        chatViewModel.stopListeningForMessages()
    }

    private fun sendMessage() {
        // Create a message
        val timeStamp = System.currentTimeMillis()
        val text = binding.massageInputText.text.toString()
        val senderId = currentUser?.phoneNumber.toString()
        val receiverId = currentFriendId
        val imageUrl = ""
        val status = MessageState.SENDING

        // Delete from textEditView
        binding.massageInputText.setText("")

        val message = FirebaseMessage(
            timeStamp = timeStamp,
            text = text,
            senderId = senderId,
            receiverId = receiverId,
            imageUrl = imageUrl,
            state = status
        )

        // Check if user chats for first time or not
        if (messages.value?.size == 0) {
            val friend = FirebaseChat(
                currentFriendId,
                currentFriendName,
                "",
                "",
                text,
                timeStamp,
                0
            )

            // add to the current user
            chatViewModel.addChat(friend.toSQLObject())
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser?.phoneNumber.toString())
                .collection("friends")
                .document(currentFriendId)
                .set(friend).addOnSuccessListener {
                    Log.d("Chats", "Added to the sender side")

                    val user = FirebaseChat(
                        currentUser?.phoneNumber.toString(),
                        "",
                        "",
                        "",
                        text,
                        timeStamp,
                        0
                    )

                    FirebaseFirestore.getInstance().collection("users")
                        .document(currentFriendId)
                        .collection("friends")
                        .document(currentUser?.phoneNumber.toString())
                        .set(user).addOnSuccessListener {
                            Log.d("Chats", "Added to the receiver side")
                        }.addOnFailureListener {
                            Log.d("Chats", "Failed to add the receiver side")
                        }

                }.addOnFailureListener {
                    Log.d("Chats", "Failed to add the sender side")
                }
        }

        chatViewModel.sendMessage(message)
    }

}
