package com.example.talkspace.repositories

import androidx.room.*
import com.example.talkspace.model.SQLiteContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {
    @Query("select * from contacts order by contactName")
    fun getContacts(): Flow<List<SQLiteContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: SQLiteContact)

    @Delete
    suspend fun delete(contact: SQLiteContact)
}