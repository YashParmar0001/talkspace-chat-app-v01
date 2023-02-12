package com.example.talkspace.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talkspace.model.SQLChat
import com.example.talkspace.model.SQLiteContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastTimeStamp DESC")
    fun getChats(): Flow<List<SQLChat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: SQLChat)

    @Query("DELETE FROM chats WHERE phoneNumber=:phoneNumber")
    suspend fun delete(phoneNumber: String)

    @Query("SELECT * FROM contacts WHERE contactPhoneNumber=:phoneNumber")
    fun checkContact(phoneNumber: String): SQLiteContact

//    @Query("SELECT * FROM chats WHERE phoneNumber=:phoneNumber")
//    fun getChat(phoneNumber: String): SQLChat
}