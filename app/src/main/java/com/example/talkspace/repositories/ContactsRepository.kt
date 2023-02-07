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

    fun getContacts(): LiveData<List<SQLiteContact>> {
        return contactsDao.getContacts().asLiveData()
    }

    fun startListeningForContacts(coroutineScope: CoroutineScope) {
        Log.d("Contacts", "Start listening for contacts...")

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
                                        contactData["contactAbout"].toString(),
                                        "",
                                        contactData["appUser"].toString().toBoolean()
                                    )

                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactsDao.insert(contact.toSQLObject())
                                        Log.d("Contact", "Contact inserted: ${contact.contactName}")
                                    }

                                }
                                DocumentChange.Type.MODIFIED -> {
                                    val contactData = dc.document.data
                                    val contact = FirebaseContact(
                                        contactData["contactId"].toString(),
                                        contactData["contactName"].toString(),
                                        contactData["contactAbout"].toString(),
                                        "",
                                        contactData["appUser"].toString().toBoolean()
                                    )

                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactsDao.insert(contact.toSQLObject())
                                        Log.d("Contact", "Contact updated: ${contact.contactName}")
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    val contactData = dc.document.data
                                    val contact = FirebaseContact(
                                        contactData["contactId"].toString(),
                                        contactData["contactName"].toString(),
                                        "",
                                        "",
                                        true
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactsDao.delete(contact.toSQLObject())
                                        Log.d("Contact", "Contact deleted: ${contact.contactName}")
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

    fun syncContacts(firestore: FirebaseFirestore, contentResolver: ContentResolver) {
        Log.d("Contact", "Syncing contacts...")
        val deviceContacts = getDeviceContacts(contentResolver)
        val serverContacts = mutableListOf<SQLiteContact>()

        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .get().addOnSuccessListener { snapshot ->
                for (dc in snapshot) {
                    val data = dc.data
                    val contact = SQLiteContact(
                        data["contactId"].toString(),
                        data["contactName"].toString(),
                        data["contactAbout"].toString(),
                        data["contactPhotoUrl"].toString(),
                        data["appUser"].toString().toBoolean()
                    )
                    serverContacts.add(contact)
                }

                for (contact in deviceContacts) {
                    val serverContact = serverContacts.find { contact.contactId == it.contactId }
                    // Check if contact uses the app or not
                    firestore.collection("users")
                        .document(contact.contactId)
                        .get().addOnSuccessListener { contactSnapshot ->
                            if (contactSnapshot.exists()) {
                                contact.isAppUser = true
                                contact.contactAbout = contactSnapshot["userAbout"].toString()
                            }
                            Log.d("Contact", "${contact.contactName} : ${contact.isAppUser}")
                            if (serverContact != null) {
                                if (areDifferent(contact, serverContact)) {
                                    updateContactOnServer(contact, firestore)
                                }
                            } else {
                                addContactToServer(contact, firestore)
                            }
                        }.addOnFailureListener {
                            Log.d("Contact", "Failed to get contact info from server")
                        }
                }

                for (contact in serverContacts) {
                    if (!deviceContacts.any { it.contactId == contact.contactId }) {
                        deleteContactOnServer(contact, firestore)
                    }
                }
            }.addOnFailureListener {
                Log.d("Contact", "Failed to get contacts from server", it)
            }
    }

    private fun areDifferent(contact1: SQLiteContact, contact2: SQLiteContact): Boolean {
        return contact1.contactName != contact2.contactName
                || contact1.isAppUser != contact2.isAppUser
                || contact1.contactAbout != contact2.contactAbout
    }

    private fun addContactToServer(contact: SQLiteContact, firestore: FirebaseFirestore) {
        firestore.collection("users")
            .document(com.example.talkspace.ui.currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactId)
            .set(contact).addOnSuccessListener {
                Log.d("Contact", "Contact added on server: ${contact.contactName}")
            }.addOnFailureListener {
                Log.d("Contact", "Failed to add contact on server", it)
            }
    }

    private fun updateContactOnServer(contact: SQLiteContact, firestore: FirebaseFirestore) {
        Log.d("UpdateContact", "Updating contact: ${contact.contactName} to ${contact.isAppUser}")
        val updates = mapOf(
            "contactName" to contact.contactName,
            "appUser" to contact.isAppUser,
            "contactAbout" to contact.contactAbout
        )

        firestore.collection("users")
            .document(com.example.talkspace.ui.currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactId)
            .update(updates).addOnSuccessListener {
                Log.d("Contact", "Contact updated on server: ${contact.contactName}")
            }.addOnFailureListener {
                Log.d("Contact", "Failed to update contact on server", it)
            }
    }

    private fun deleteContactOnServer(contact: SQLiteContact, firestore: FirebaseFirestore) {
        firestore.collection("users")
            .document(com.example.talkspace.ui.currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactId)
            .delete().addOnSuccessListener {
                Log.d("Contact", "Contact deleted from server successfully: ${contact.contactName}")
            }.addOnFailureListener {
                Log.d("Contact", "Failed to delete contact from server", it)
            }
    }

    @SuppressLint("Range")
    fun getDeviceContacts(contentResolver: ContentResolver): List<SQLiteContact> {
        // Getting all the contacts from the phone
        val contacts = mutableListOf<SQLiteContact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor == null) {
            Log.d("Contacts", "Contact cursor is null")
        } else {
            while (cursor.moveToNext()) {
                // Get contact info using cursor
                val contactId = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val contactName = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                )
                val contactPhoneNumber = getPhoneNumber(contactId, contentResolver)

                Log.d("Contact", "Id: $contactId | Name: $contactName | Phone: $contactPhoneNumber")

                contacts.add(
                    SQLiteContact(
                        contactPhoneNumber,
                        contactName,
                        "",
                        "",
                        false
                    )
                )
            }
        }
        cursor?.close()
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