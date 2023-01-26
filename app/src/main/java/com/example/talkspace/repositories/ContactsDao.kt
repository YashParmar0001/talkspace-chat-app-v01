package com.example.talkspace.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talkspace.model.SQLiteContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {
    @Query("select * from contacts order by contactName")
    fun getContacts(): Flow<List<SQLiteContact>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: SQLiteContact)
}