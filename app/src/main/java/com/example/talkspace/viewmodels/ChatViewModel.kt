package com.example.talkspace.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.example.talkspace.model.FirebaseMessage
import com.example.talkspace.model.SQLChat
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.model.SQLiteMessage
import com.example.talkspace.repositories.ChatRepository
import com.example.talkspace.repositories.ContactsRepository
import com.example.talkspace.ui.currentUser

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository
): ViewModel() {
    // Get all the chats from database
    val chats = chatRepository.getChats()

    private val _currentFriendId = MutableLiveData<String>()
    val currentFriendId: LiveData<String> = _currentFriendId

    private val _currentFriendName = MutableLiveData<String>()
    val currentFriendName : LiveData<String> = _currentFriendName

    private val _currentFriendAbout = MutableLiveData<String>()
    val currentFriendAbout : LiveData<String> = _currentFriendAbout

    fun sendMessage(message: FirebaseMessage) {
        chatRepository.sendMessage(currentFriendId.value.toString(), message, viewModelScope)
    }

    fun setCurrentFriendId(friendId: String) {
        // This will set id as a phone number
        _currentFriendId.value = friendId
    }

    fun setCurrentFriendName(friendName: String) {
        _currentFriendName.value = friendName
    }

    fun setCurrentFriendAbout(friendAbout: String) {
        _currentFriendAbout.value = friendAbout
    }

    fun getMessages(friendId: String): LiveData<List<SQLiteMessage>> {
        return chatRepository.getMessages(friendId)
    }

    fun getContacts(): LiveData<List<SQLiteContact>> {
        return contactsRepository.getContacts()
    }

    fun addChat(chat: SQLChat) {
        chatRepository.addChat(chat, viewModelScope)
    }

    fun addContact(contact: SQLiteContact) {
        contactsRepository.addContact(contact, viewModelScope)
    }

    fun startListeningForMessages() {
        chatRepository.startListeningForMessages(
            currentUser?.phoneNumber.toString(),
            currentFriendId.value.toString(),
            viewModelScope
        )
    }

    fun stopListeningForMessages() {
        chatRepository.stopListeningForMessages()
    }

    fun startListeningForChats(context: Context) {
        chatRepository.startListeningForChats(viewModelScope, context)
    }

    fun stopListeningForChats() {
        chatRepository.stopListeningForChats()
    }

    fun startListeningForContacts() {
        contactsRepository.startListeningForContacts(viewModelScope)
    }

    fun stopListeningForContacts() {
        contactsRepository.stopListeningForContacts()
    }
}

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, contactsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}