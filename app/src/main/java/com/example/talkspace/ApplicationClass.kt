package com.example.talkspace

import android.app.Application
import com.example.talkspace.repositories.AppDatabase
import com.example.talkspace.repositories.ChatRepository
import com.example.talkspace.repositories.ContactsRepository

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
}