package com.example.talkspace.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.example.talkspace.model.FirebaseMessage
import com.example.talkspace.model.SQLChat
import com.example.talkspace.model.SQLiteMessage
import com.example.talkspace.repositories.ChatRepository
import com.example.talkspace.ui.currentUser

class ChatViewModel(
    private val repository: ChatRepository
): ViewModel() {
    // Get all the chats from database
    val chats = repository.getChats()

    private val _currentFriendId = MutableLiveData<String>()
    val currentFriendId: LiveData<String> = _currentFriendId

    private val _currentFriendName = MutableLiveData<String>()
    val currentFriendName : LiveData<String> = _currentFriendName

    fun sendMessage(message: FirebaseMessage) {
        repository.sendMessage(currentFriendId.value.toString(), message, viewModelScope)
    }

    fun setCurrentFriendId(friendId: String) {
        // This will set id as a phone number
        _currentFriendId.value = friendId
    }

    fun setCurrentFriendName(friendName: String) {
        _currentFriendName.value = friendName
    }

    fun getMessages(friendId: String): LiveData<List<SQLiteMessage>> {
        return repository.getMessages(friendId)
    }

    fun addChat(chat: SQLChat) {
        repository.addChat(chat, viewModelScope)
    }

    fun startListeningForMessages() {
        repository.startListeningForMessages(
            currentUser?.phoneNumber.toString(),
            currentFriendId.value.toString(),
            viewModelScope
        )
    }

    fun stopListeningForMessages() {
        repository.stopListeningForMessages()
    }

    fun startListeningForChats(context: Context) {
        repository.startListeningForChats(viewModelScope, context, currentFriendId.value.toString())
    }

    fun stopListeningForChats() {
        repository.stopListeningForChats()
    }

    fun updateNameIfExists(context: Context) {
        repository.returnNameIfExists(currentFriendId.value.toString(), context)
    }
}

class ChatViewModelFactory(private val repository: ChatRepository): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}