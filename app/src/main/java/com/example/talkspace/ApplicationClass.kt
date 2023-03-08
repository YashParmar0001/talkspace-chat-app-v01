package com.example.talkspace

import android.app.Application
import com.example.talkspace.repositories.AppDatabase
import com.example.talkspace.repositories.ChatRepository
import com.example.talkspace.repositories.ContactsRepository
import com.example.talkspace.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApplicationClass: Application() {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(database.chatDao(), database.messageDao())
    }

    val contactRepository: ContactsRepository by lazy {
        ContactsRepository(database.contactDao())
    }

    val userRepository: UserRepository by lazy {
        UserRepository(this.applicationContext)
    }

    override fun onCreate() {
        super.onCreate()

        // Apply settings to the firebase
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
//        contactRepository.notifyAppUserContactsAboutStatus("Using the app")
    }
}