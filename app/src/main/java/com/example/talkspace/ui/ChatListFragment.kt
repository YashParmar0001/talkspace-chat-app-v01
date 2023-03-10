package com.example.talkspace.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.MainActivity
import com.example.talkspace.R
import com.example.talkspace.adapter.ChatListAdapter
import com.example.talkspace.adapter.StatusAdapter
import com.example.talkspace.databinding.FragmentChatListBinding
import com.example.talkspace.viewmodels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : Fragment() {
    private lateinit var binding: FragmentChatListBinding
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val fragmentBinding = DataBindingUtil.inflate<FragmentChatListBinding>(
            inflater,
            R.layout.fragment_chat_list,
            container,
            false
        ).apply {
            viewModel = chatViewModel
            chatListFragment = this@ChatListFragment
            lifecycleOwner = viewLifecycleOwner
        }

        binding = fragmentBinding

        (requireActivity() as MainActivity).showBottomNavigation()

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

    }

    fun goToUserDetailsFragment() {
        view?.findNavController()?.navigate(R.id.action_chatListFragment_to_userDetailFragment)
    }

    fun goToContactsOnAppFragment() {
        view?.findNavController()?.navigate(R.id.action_chatListFragment_to_contactsOnAppFragment)
    }

    fun goBack() {
        view?.findNavController()?.navigateUp()
    }
}