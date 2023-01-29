package com.example.talkspace.repositories

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.talkspace.model.FirebaseContact
import com.example.talkspace.model.SQLiteContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsRepository(
    private val contactsDao: ContactsDao
) {
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val firestore = FirebaseFirestore.getInstance()

    private var registration: ListenerRegistration? = null

    fun addContact(contact: SQLiteContact, coroutineScope: CoroutineScope) {
        if (currentUser == null) {
            return
        }

        // Add contact to the firebase
        firestore.collection("users")
            .document(currentUser.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactId)
            .set(contact.toFirebaseObject())
            .addOnSuccessListener {
                Log.d("Contacts", "Contact uploaded successfully")
                // Now add to the SQLite database
                coroutineScope.launch(Dispatchers.IO) {
                    contactsDao.insert(contact)
                }
            }.addOnFailureListener {
                Log.d("Contacts", "Failed to upload contact", it)
            }
    }

    fun getContacts(): LiveData<List<SQLiteContact>> {
        return contactsDao.getContacts().asLiveData()
    }

    fun startListeningForContacts(coroutineScope: CoroutineScope) {
        Log.d("Contacts", "Start listening for contacts...")
//        val settings = FirebaseFirestoreSettings.Builder()
//            .setPersistenceEnabled(false)
//            .build()
//        firestore.firestoreSettings = settings

        registration = firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d("Contacts", "Failed to listening contacts", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    if (snapshot.metadata.isFromCache) {
                        Log.d("Contacts", "Contact data is coming from cache")
                    }else {
                        for (dc in snapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val contactData = dc.document.data
                                    val contact = FirebaseContact(
                                        contactData["contactId"].toString(),
                                        contactData["contactName"].toString(),
                                        "",
                                        "",
                                        true
                                    )

                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactsDao.insert(contact.toSQLObject())
                                    }
                                }
                                else -> {
                                    Log.d("Contacts", "Other operations done")
                                }
                            }
                        }
                    }
                }
            }
    }

    fun stopListeningForContacts() {
        Log.d("Contacts", "Stopping listener for contacts...")
        registration?.remove()
        registration = null
    }

//    fun checkIfAppUser(contactNumber: String): Boolean {
//        firestore.collection("users")
//            .document(contactNumber)
//            .get().addOnSuccessListener {
//
//            }
//    }

    @SuppressLint("Range")
    fun getAllContacts(contentResolver: ContentResolver, coroutineScope: CoroutineScope): List<SQLiteContact> {
        // Getting all the contacts from the phone
        val contacts = mutableListOf<SQLiteContact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null)

        if (cursor == null) {
            Log.d("Contacts", "Contact cursor is null")
        }else {
            while (cursor.moveToNext()) {
                // Get contact info using cursor
                val contactId = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val contactName = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                )
                val contactPhoneNumber = getPhoneNumber(contactId, contentResolver)

                contacts.add(SQLiteContact(
                    contactPhoneNumber,
                    contactName,
                    "",
                    "",
                    false
                ))
            }
        }
        cursor?.close()

        // Now add all contacts to the SQLite database
        for (contact in contacts) {
            // Check if contact uses the app or not and add to database accordingly
            firestore.collection("users")
                .document(contact.contactId)
                .get().addOnSuccessListener { snapshot ->
                    coroutineScope.launch(Dispatchers.IO) {
                        contact.isAppUser = snapshot.exists()
                        contactsDao.insert(contact)
                    }
                }
        }
        return contacts
    }

    @SuppressLint("Range")
    private fun getPhoneNumber(contactId: String, contentResolver: ContentResolver): String {
        var phoneNumber = ""
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        if ((phoneCursor?.count ?: 0) > 0) {
            while (phoneCursor != null && phoneCursor.moveToNext()) {
                phoneNumber = phoneCursor.getString(
                    phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            }
            phoneCursor?.close()
        }
        phoneNumber = phoneNumber.replace(" ", "")
        if (!phoneNumber.contains("+91")) {
            phoneNumber = "+91$phoneNumber"
        }
        return phoneNumber
    }
}