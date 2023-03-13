package com.example.talkspace.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.talkspace.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
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
        val num1 = number1.replace("+91", "").toLong()
        val num2 = number2.replace("+91", "").toLong()
        return if (num1 > num2) {
            "$num1-$num2"
        }else if (num1 < num2) {
            "$num2-$num1"
        }else {
            number1
        }
    }

    fun startListeningForMessages(userId: String, currentFriendId: String, coroutineScope: CoroutineScope) {
        Log.d("Chats", "Starting listening for messages...")
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

    fun startListeningForChats(coroutineScope: CoroutineScope, contacts: LiveData<List<SQLiteContact>>) {
        Log.d("Chats", "Starting listener for chats...")
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
                                    val data = dc.document.data
                                    val chat = FirebaseChat(
                                        data["phoneNumber"].toString(),
                                        data["friendName"].toString(),
                                        data["friendAbout"].toString(),
                                        data["friendPhotoUrl"].toString(),
                                        data["lastChat"].toString(),
                                        data["lastTimeStamp"].toString().toLong(),
                                        0
                                    )

//                                    val contact = contacts.value?.find {
//                                        it.contactPhoneNumber == chat.phoneNumber
//                                    }

                                    coroutineScope.launch(Dispatchers.IO) {
                                        val contact = chatDao.checkContact(chat.phoneNumber)
                                        Log.d("ContactData", "List: $contacts")
                                        Log.d("ContactData", "Chat data: $contact")

                                        if (contact != null) {
                                            firestore.collection("users")
                                                .document(currentUser?.phoneNumber.toString())
                                                .collection("friends")
                                                .document(chat.phoneNumber)
                                                .update("friendName", contact.contactName)
                                                .addOnSuccessListener {
                                                    Log.d("ChatUpdate", "Chat name updated: ${contact.contactName}")
                                                }.addOnFailureListener {
                                                    Log.d("ChatUpdate", "Failed to update chat: ${contact.contactName}")
                                                }
                                        }else {
                                            Log.d("UpdateChat", "Adding chat: ${chat.friendName}")
                                            chatDao.insert(chat.toSQLObject())
                                        }
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    val data = dc.document.data
                                    Log.d("UpdateChat", "Chat updated")
                                    val newChat = FirebaseChat(
                                        data["phoneNumber"].toString(),
                                        data["friendName"].toString(),
                                        data["friendAbout"].toString(),
                                        data["friendPhotoUrl"].toString(),
                                        data["lastChat"].toString(),
                                        data["lastTimeStamp"].toString().toLong(),
                                        0
                                    )
                                    Log.d("Chat", "Remaining: " +
                                            data["remainingMessages"].toString()
                                    )

                                    coroutineScope.launch(Dispatchers.IO) {
                                        Log.d("UpdateChat", "Updating chat: ${newChat.friendName}")
                                        chatDao.insert(newChat.toSQLObject())
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    Log.d("UpdateChat", "Chat deleted")
                                    val data = dc.document.data

                                    coroutineScope.launch(Dispatchers.IO) {
                                        chatDao.delete(data["phoneNumber"].toString())
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

}