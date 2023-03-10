package com.example.talkspace.repositories

import androidx.room.*
import com.example.talkspace.model.SQLiteMessage
import dagger.Provides
import dagger.hilt.InstallIn
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("select * from messages where receiverId =:receiverId or senderId =:receiverId order by timeStamp")
    fun getMessages(receiverId: String): Flow<List<SQLiteMessage>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: SQLiteMessage)
}