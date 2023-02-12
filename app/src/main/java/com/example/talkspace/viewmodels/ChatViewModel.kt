package com.example.talkspace.viewmodels

import android.content.ContentResolver
import android.content.Context
import androidx.lifecycle.*
import com.example.talkspace.model.FirebaseMessage
import com.example.talkspace.model.SQLChat
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.model.SQLiteMessage
import com.example.talkspace.repositories.ChatRepository
import com.example.talkspace.repositories.ContactsRepository
import com.example.talkspace.ui.currentUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope

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

    private val _appUserContacts = contactsRepository.getAppUserContacts()
    val appUserContacts: LiveData<List<SQLiteContact>> = _appUserContacts

    private val _nonAppUserContacts = contactsRepository.getNonAppUserContacts()
    val nonAppUserContacts: LiveData<List<SQLiteContact>> = _nonAppUserContacts

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

    fun getCurrentFriendName(): String? {
        return currentFriendName.value
    }

    fun getMessages(friendId: String): LiveData<List<SQLiteMessage>> {
        return chatRepository.getMessages(friendId)
    }

    fun getContacts(): LiveData<List<SQLiteContact>> {
        return contactsRepository.getContacts()
    }

//    fun getAppUserContacts(): LiveData<List<SQLiteContact>> {
//        return contactsRepository.getAppUserContacts()
//    }

//    fun getNonAppUserContacts(): LiveData<List<SQLiteContact>> {
//        return contactsRepository.getNonAppUserContacts()
//    }

    fun addChat(chat: SQLChat) {
        chatRepository.addChat(chat, viewModelScope)
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

    fun startListeningForChats() {
        chatRepository.startListeningForChats(viewModelScope, appUserContacts)
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

    fun syncContacts(
        firestore: FirebaseFirestore,
        contentResolver: ContentResolver,
        isFirstTimeLogin: Boolean
    ) {
        contactsRepository.syncContacts(firestore, contentResolver, isFirstTimeLogin)
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