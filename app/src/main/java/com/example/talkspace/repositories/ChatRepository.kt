package com.example.talkspace.repositories

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.talkspace.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) {
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val firestore = FirebaseFirestore.getInstance()

    private var messageRegistration: ListenerRegistration? = null
    private var chatRegistration: ListenerRegistration? = null

    fun getChats(): LiveData<List<SQLChat>> {
        return chatDao.getChats().asLiveData()
    }

    fun getMessages(friendId: String): LiveData<List<SQLiteMessage>> {
        return messageDao.getMessages(friendId).asLiveData()
    }

    fun addChat(chat: SQLChat, coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            chatDao.insert(chat)
        }
    }

    fun sendMessage(friendId: String, message: FirebaseMessage, coroutineScope: CoroutineScope) {
        if (currentUser == null) {
            return
        }

        val currentUserId = currentUser.phoneNumber.toString()
        val chatKey = generateChatKey(friendId, currentUserId)
        // Add message to the firebase and SQL
        firestore.collection("chats")
            .document(chatKey)
            .collection("messages")
            .document(message.timeStamp.toString())
            .set(message)
            .addOnSuccessListener {
                Log.d("ChatRepository", "Message sent successfully")
                // After the message successfully added to the firebase add it to the SQL database
                coroutineScope.launch(Dispatchers.IO) {
                    messageDao.insert(message.toSQLObject())
                }

                // Todo: Add notifications here
            }.addOnFailureListener {
                Log.d("ChatRepository", "Message not sent successfully")
            }
    }

    private fun generateChatKey(number1: String, number2: String): String {
        val number1_ = number1.replace("+91", "").toLong()
        val number2_ = number2.replace("+91", "").toLong()
        return if (number1_ > number2_) {
            "$number1_-$number2_"
        }else if (number1_ < number2_) {
            "$number2_-$number1_"
        }else {
            number1
        }
    }

    fun startListeningForMessages(userId: String, currentFriendId: String, coroutineScope: CoroutineScope) {
        Log.d("Chats", "Starting listening for messages...")
//        val settings = FirebaseFirestoreSettings.Builder()
//            .setPersistenceEnabled(false)
//            .build()
//        firestore.firestoreSettings = settings
        val chatKey = generateChatKey(userId, currentFriendId)
        messageRegistration = firestore.collection("chats")
            .document(chatKey)
            .collection("messages")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d("Chats", "Error listening for messages")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    if (snapshot.metadata.isFromCache) {
                        Log.d("Chats", "Message listener data coming from cache")
                    }else {
                        for (dc in snapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    Log.d("Chats", "New message added")

                                    val userData = dc.document.data

                                    val message = SQLiteMessage(
                                        userData["timeStamp"].toString().toLong(),
                                        userData["text"].toString(),
                                        userData["senderId"].toString(),
                                        userData["receiverId"].toString(),
                                        userData["imageUrl"].toString(),
                                        MessageState.valueOf("RECEIVED")
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        messageDao.insert(message)
                                    }
                                }
                                else -> {
                                    Log.d("Chats", "Other operation done")
                                }
                            }
                        }
                    }
                }
            }
    }

    fun stopListeningForMessages() {
        Log.d("Chats", "Stopping listening for messages...")
        messageRegistration?.remove()
        messageRegistration = null
    }

    fun startListeningForChats(coroutineScope: CoroutineScope, context: Context) {
        Log.d("Chats", "Starting listener for chats...")
//        val settings = FirebaseFirestoreSettings.Builder()
//            .setPersistenceEnabled(false)
//            .build()
//        firestore.firestoreSettings = settings
        chatRegistration = firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("friends")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d("Chats", "Error occurred while listening to the chats", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    if (snapshot.metadata.isFromCache) {
                        Log.d("Chats", "Chat listener data coming from cache")
                    }else {
                        for (dc in snapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    Log.d("Chats", "New chat added ${dc.document.data}")
                                    val userData = dc.document.data

                                    // Check if user is present in contacts or not
                                    val newFriendId = userData["phoneNumber"].toString()
                                    val newFriendName = returnNameIfExists(newFriendId, context)
                                    Log.d("Chats", "New friend name: $newFriendName")

                                    val chat = SQLChat(
                                        userData["phoneNumber"].toString(),
                                        newFriendName,
                                        userData["friendAbout"].toString(),
                                        userData["friendPhotoUrl"].toString(),
                                        userData["lastChat"].toString(),
                                        userData["lastTimeStamp"].toString().toLong(),
                                        0
                                    )

                                    coroutineScope.launch(Dispatchers.IO) {
                                        chatDao.insert(chat)
                                    }

                                    val contact = FirebaseContact(
                                        newFriendId,
                                        newFriendName,
                                        "",
                                        ""
                                    )

                                    firestore.collection("users")
                                        .document(currentUser?.phoneNumber.toString())
                                        .collection("contacts")
                                        .document(newFriendId)
                                        .set(contact)
                                        .addOnSuccessListener {
                                            Log.d("Contacts", "Contact added successfully")

                                        }.addOnFailureListener {
                                            Log.d("Contacts", "Failed to add contact", it)
                                        }

                                    if (newFriendName != newFriendId) {
                                        firestore.collection("users")
                                            .document(currentUser?.phoneNumber.toString())
                                            .collection("friends")
                                            .document(newFriendId)
                                            .update("friendName", newFriendName)
                                            .addOnSuccessListener {
                                                Log.d("Chats", "New chat's name updated")
                                            }.addOnFailureListener {
                                                Log.d("Chats", "Error updating new chat name", it)
                                            }
                                    }
                                }
                                else -> {
                                    Log.d("Chats", "Other operations done")
                                }
                            }
                        }
                    }
                }
            }
    }

    fun stopListeningForChats() {
        Log.d("Chats", "Stopping listening for chats...")
        chatRegistration?.remove()
        chatRegistration = null
    }

    fun returnNameIfExists(friendId: String, context: Context): String {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(friendId))
        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.NUMBER,
            ContactsContract.PhoneLookup.DISPLAY_NAME
        )

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
                val displayIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                var contactId = ""
                var contactName = ""
                if (idIndex >= 0) contactId = cursor.getString(idIndex)
                if (displayIndex >= 0) contactName = cursor.getString(displayIndex)

                Log.d("Chats", "id: $contactId | name: $contactName")
                cursor.close()
                return contactName
            }else {
                Log.d("Chats", "Contact not exists")
                cursor.close()
                return friendId
            }
        }else {
            Log.d("Chats", "Cursor is null")
            return friendId
        }
    }

}