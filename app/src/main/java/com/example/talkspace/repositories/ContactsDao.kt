package com.example.talkspace.repositories

import androidx.room.*
import com.example.talkspace.model.SQLiteContact
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Dao
interface ContactsDao {
    @Query("select * from contacts order by contactName")
    fun getContacts(): Flow<List<SQLiteContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: SQLiteContact)

    @Query("delete from contacts where contactPhoneNumber=:phoneNumber")
    suspend fun delete(phoneNumber: String)

    @Query("select * from contacts where isAppUser = 1 order by contactName")
    fun getAppUserContacts(): Flow<List<SQLiteContact>>

    @Query("select * from contacts where isAppUser = 0 order by contactName")
    fun getNonAppUserContacts(): Flow<List<SQLiteContact>>
}