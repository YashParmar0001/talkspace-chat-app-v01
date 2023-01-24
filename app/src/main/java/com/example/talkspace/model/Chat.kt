package com.example.talkspace.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class FirebaseChat(
    val phoneNumber: String,
    val friendName: String,
    val friendAbout: String,
    val friendPhotoUrl: String,
    val lastChat: String,
    val lastTimeStamp: Long,
    val remainingMessages: Int,
) {
    fun toSQLObject(): SQLChat {
        return SQLChat(
            phoneNumber = phoneNumber,
            friendName = friendName,
            friendAbout = friendAbout,
            friendPhotoUri = friendPhotoUrl,
            lastChat = lastChat,
            lastTimeStamp = lastTimeStamp,
            remainingMessages = remainingMessages
        )
    }
}

@Entity(tableName = "chats")
data class SQLChat(
    @PrimaryKey val phoneNumber: String,
    @ColumnInfo(name = "friendName") val friendName: String,
    @ColumnInfo(name = "friendAbout") val friendAbout: String,
    @ColumnInfo(name = "friendPhotoUri") val friendPhotoUri: String,
    @ColumnInfo(name = "lastChat") val lastChat: String,
    @ColumnInfo(name = "lastTimeStamp") val lastTimeStamp: Long,
    @ColumnInfo(name = "remainingMessages") val remainingMessages: Int
)