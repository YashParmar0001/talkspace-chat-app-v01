package com.example.talkspace.observers

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.util.Log
import com.example.talkspace.viewmodels.ChatViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsChangeObserver(
    handler: Handler,
    private val coroutineScope: CoroutineScope,
    private val contentResolver: ContentResolver,
    private val chatViewModel: ChatViewModel
) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        coroutineScope.launch(Dispatchers.IO) {
            Log.d("Contact", "Syncing contacts...")
            chatViewModel.syncContacts(FirebaseFirestore.getInstance(), contentResolver)
            Log.d("Contact", "Contact synced successfully")
        }
    }
}

