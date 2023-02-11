package com.example.talkspace.repositories

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.talkspace.model.FirebaseContact
import com.example.talkspace.model.SQLiteContact
import com.google.android.gms.tasks.Task
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

    fun getAppUserContacts(): LiveData<List<SQLiteContact>> {
        return contactsDao.getAppUserContacts().asLiveData()
    }

    fun getNonAppUserContacts(): LiveData<List<SQLiteContact>> {
        return contactsDao.getNonAppUserContacts().asLiveData()
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
                                    Log.d("UpdateContact", "Contact added to server")
                                    val contactData = dc.document.data
                                    val contact = FirebaseContact(
                                        contactData["contactId"].toString(),
                                        contactData["contactName"].toString(),
                                        contactData["contactAbout"].toString(),
                                        "", // TODO: Photo update
                                        contactData["appUser"].toString().toBoolean()
                                    )

                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactsDao.insert(contact.toSQLObject())
                                        Log.d("Contact", "Contact inserted: ${contact.contactName}")
                                    }

                                }
                                DocumentChange.Type.MODIFIED -> {
                                    Log.d("UpdateContact", "Contact modified on server")
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

                                    // Update respective chat if exists
                                    if (contact.isAppUser) {
                                        val updates = mapOf(
                                            "friendAbout" to contact.contactAbout,
                                            "friendName" to contact.contactName,
                                            "friendPhotoUrl" to contact.contactPhotoUrl
                                        )
                                        firestore.collection("users")
                                            .document(currentUser?.phoneNumber.toString())
                                            .collection("friends")
                                            .document(contact.contactPhoneNumber)
                                            .update(updates).addOnSuccessListener {
                                                Log.d("ChatUpdates", "Chat updated: $updates")
                                            }.addOnFailureListener {
                                                Log.d("ChatUpdates", "Failed to update chat: $updates")
                                            }
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    val contactData = dc.document.data
                                    val contact = FirebaseContact(
                                        contactData["contactId"].toString(),
                                        contactData["contactName"].toString(),
                                        "",
                                        "",
                                        false
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

    fun syncContacts(
        firestore: FirebaseFirestore,
        contentResolver: ContentResolver,
        isFirstTimeLogin: Boolean
    ) {
        Log.d("Contact", "Syncing contacts...")
        val deviceContacts = getDeviceContacts(contentResolver)
        val serverContacts = mutableListOf<SQLiteContact>()

        var remainingContacts = deviceContacts.size

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
                    val serverContact = serverContacts.find { contact.contactPhoneNumber == it.contactPhoneNumber }
                    // Check if contact uses the app or not
                    firestore.collection("users")
                        .document(contact.contactPhoneNumber)
                        .get().addOnSuccessListener { contactSnapshot ->
                            if (contactSnapshot.exists()) {
                                contact.isAppUser = true
                                contact.contactAbout = contactSnapshot["userAbout"].toString()
                            }
                            Log.d("Contact", "${contact.contactName} : ${contact.isAppUser}")
                            if (serverContact != null) {
                                if (areDifferent(contact, serverContact)) {
                                    updateContactOnServer(contact, firestore).addOnSuccessListener {
                                        Log.d("Contact", "Contact updated on server: ${contact.contactName}")
                                        remainingContacts--

                                        // If all contacts update is done then notify
                                        if (isFirstTimeLogin) {
                                            if (remainingContacts == 0) {
                                                Log.d("NotifyContact", "First time login")
                                                notifyAppUserContacts()
                                            }
                                        }
                                    }.addOnFailureListener {
                                        Log.d("Contact", "Failed to update contact on server", it)
                                    }
                                }else {
                                    remainingContacts--
                                    // If all contacts update is done then notify
                                    if (isFirstTimeLogin) {
                                        if (remainingContacts == 0) {
                                            Log.d("NotifyContact", "First time login")
                                            notifyAppUserContacts()
                                        }
                                    }
                                }
                            } else {
                                addContactToServer(contact, firestore).addOnSuccessListener {
                                    Log.d("Contact", "Contact added on server: ${contact.contactName}")
                                    remainingContacts--
                                    // If all contacts update is done then notify
                                    if (isFirstTimeLogin) {
                                        if (remainingContacts == 0) {
                                            Log.d("NotifyContact", "First time login")
                                            notifyAppUserContacts()
                                        }
                                    }
                                }.addOnFailureListener {
                                    Log.d("Contact", "Failed to add contact on server", it)
                                }
                            }
                        }.addOnFailureListener {
                            Log.d("Contact", "Failed to get contact info from server")
                        }
                }

                for (contact in serverContacts) {
                    if (!deviceContacts.any { it.contactPhoneNumber == contact.contactPhoneNumber }) {
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

    private fun addContactToServer(contact: SQLiteContact, firestore: FirebaseFirestore): Task<Void> {
        return firestore.collection("users")
            .document(com.example.talkspace.ui.currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactPhoneNumber)
            .set(contact)
    }

    private fun updateContactOnServer(contact: SQLiteContact, firestore: FirebaseFirestore): Task<Void> {
        Log.d("UpdateContact", "Updating contact: ${contact.contactName} to ${contact.isAppUser}")
        val updates = mapOf(
            "contactName" to contact.contactName,
            "appUser" to contact.isAppUser,
            "contactAbout" to contact.contactAbout
        )

        return firestore.collection("users")
            .document(com.example.talkspace.ui.currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactPhoneNumber)
            .update(updates)
    }

    private fun deleteContactOnServer(contact: SQLiteContact, firestore: FirebaseFirestore) {
        firestore.collection("users")
            .document(com.example.talkspace.ui.currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactPhoneNumber)
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

    private fun notifyAppUserContacts() {
        Log.d("NotifyContact", "Notifying contacts...")
        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .whereEqualTo("appUser", true)
            .get().addOnSuccessListener { snapshot ->
                for (dc in snapshot.documents) {
                    val data = dc.data
                    val phoneNumber = data?.get("contactPhoneNumber").toString()
                    Log.d("Contact", "update to contact: ${data?.get("contactName")?.toString()}")
                    firestore.collection("users")
                        .document(phoneNumber)
                        .collection("contacts")
                        .document(currentUser?.phoneNumber.toString())
                        .update("appUser", true)
                        .addOnSuccessListener {
                            Log.d("Contact", "Updated to contact")
                        }.addOnFailureListener {
                            Log.d("Contact", "Failed to update to contact", it)
                        }
                }
            }.addOnFailureListener {
                Log.d("Contact", "Failed to notify users", it)
            }

        // Todo: Send notification to contacts
    }
}