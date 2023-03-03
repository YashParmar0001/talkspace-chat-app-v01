package com.example.talkspace.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.ApplicationClass
import com.example.talkspace.R
import com.example.talkspace.adapter.ChatListAdapter
import com.example.talkspace.adapter.StatusAdapter
import com.example.talkspace.databinding.FragmentChatListBinding
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.google.android.material.snackbar.Snackbar

class ChatListFragment : Fragment() {

    private lateinit var binding: FragmentChatListBinding
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

        binding = FragmentChatListBinding.inflate(inflater, container, false)

        // For stories
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.recyclerView.adapter = StatusAdapter(requireContext())

        // For chats
        binding.friendRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        val adapter = ChatListAdapter(chatViewModel)
        binding.friendRecyclerView.adapter = adapter

        // Showing chats in recycler view
        chatViewModel.chats.observe(viewLifecycleOwner) { chats ->
            chats?.let {
                adapter.submitList(chats)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuBtn = getView()?.findViewById<ImageView>(R.id.menu_btn)
        val searchBtn = getView()?.findViewById<ImageView>(R.id.search_btn)
        val profileIcon = getView()?.findViewById<ImageView>(R.id.profile_icon)
        menuBtn?.setOnClickListener {
            Toast.makeText(context, "menu button will be implemented later", Toast.LENGTH_SHORT)
                .show()
        }
        searchBtn?.setOnClickListener {
            Toast.makeText(context, "Search Button will be implemented later", Toast.LENGTH_SHORT)
                .show()
        }

        profileIcon?.setOnClickListener {
            findNavController().navigate(R.id.action_chatListFragment_to_userDetailFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FriendListFragment", "Fragment Destroy")
    }
}