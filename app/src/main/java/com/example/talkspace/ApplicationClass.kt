package com.example.talkspace

import android.app.Application
import com.example.talkspace.repositories.AppDatabase
import com.example.talkspace.repositories.ChatRepository

class ApplicationClass: Application() {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: ChatRepository by lazy {
        ChatRepository(database.chatDao(), database.messageDao())
    }
}