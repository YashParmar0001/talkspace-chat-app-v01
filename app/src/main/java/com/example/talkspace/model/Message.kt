package com.example.talkspace.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class SQLiteMessage(
    @PrimaryKey val timeStamp: Long,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "senderId") val senderId: String,
    @ColumnInfo(name = "receiverId") val receiverId: String,
    @ColumnInfo(name = "imageUri") val imageUri: String,
    @ColumnInfo(name = "state") var state: MessageState
)

data class FirebaseMessage(
    val timeStamp: Long,
    val text: String,
    val senderId: String,
    val receiverId: String,
    val imageUrl: String,
    var state: MessageState
) {
    fun toSQLObject(): SQLiteMessage {
        return SQLiteMessage(
            timeStamp = timeStamp,
            text = text,
            senderId = senderId,
            receiverId = receiverId,
            imageUri = imageUrl,
            state = state
        )
    }
}

enum class MessageState {
    SENDING, SENT, RECEIVED, SEEN
}