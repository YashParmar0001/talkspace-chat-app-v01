package com.example.talkspace.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talkspace.model.SQLChat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastTimeStamp DESC")
    fun getChats(): Flow<List<SQLChat>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(chat: SQLChat)

//    @Query("SELECT * FROM chats WHERE phoneNumber=:phoneNumber")
//    fun getChat(phoneNumber: String): SQLChat
}