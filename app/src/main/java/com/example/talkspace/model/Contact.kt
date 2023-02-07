package com.example.talkspace.model

import android.view.ViewDebug.CapturedViewProperty
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class FirebaseContact(
    val contactId: String,
    val contactName: String,
    val contactAbout: String,
    val contactPhotoUrl: String,
    var isAppUser: Boolean,
) {
    fun toSQLObject(): SQLiteContact {
        return SQLiteContact(
            contactId = contactId,
            contactName = contactName,
            contactAbout = contactAbout,
            contactPhotoUrl = contactPhotoUrl,
            isAppUser = isAppUser
        )
    }
}

@Entity(tableName = "contacts")
data class SQLiteContact(
    @PrimaryKey val contactId: String,
    @ColumnInfo(name = "contactName") var contactName: String,
    @ColumnInfo(name = "contactAbout") var contactAbout: String,
    @ColumnInfo(name = "contactPhotoUrl") val contactPhotoUrl: String,
    @ColumnInfo(name = "isAppUser") var isAppUser: Boolean
) {
    fun toFirebaseObject(): FirebaseContact {
        return FirebaseContact(
            contactId = contactId,
            contactName = contactName,
            contactAbout = contactAbout,
            contactPhotoUrl = contactPhotoUrl,
            isAppUser = isAppUser
        )
    }

}