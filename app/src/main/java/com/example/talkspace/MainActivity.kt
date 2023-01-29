package com.example.talkspace

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.talkspace.databinding.ActivityMainBinding
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController : NavController

    private val chatViewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            (this.application as ApplicationClass).chatRepository,
            (this.application as ApplicationClass).contactRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser == null){
            startActivity(Intent(this,SignInActivity::class.java))
            finish()
        }else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.outer_container) as NavHostFragment

            navController = navHostFragment.navController

            // Apply settings to the firebase
            val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
            FirebaseFirestore.getInstance().firestoreSettings = settings

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val contacts = getAllContacts()
                    for (contact in contacts) {
                        Log.d("Contacts", "Name: ${contact.contactName} | Phone: ${contact.contactId}")
                    }
                }

                // Todo: Listen for changes in phone's contacts
                contentResolver.registerContentObserver(
                    ContactsContract.DeletedContacts.CONTENT_URI,
                    true,
                    ContactObserver(Handler(Looper.getMainLooper()))
                )
            }else {
                val requestPermissionLauncher =
                    registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->
                        if (isGranted) {
                            Log.i("Permission: ", "Granted")
                            lifecycleScope.launch(Dispatchers.IO) {
//                                val contacts = getAllContacts()
//                                for (contact in contacts) {
//                                    Log.d("Contacts", "Name: ${contact.contactName} | Phone: ${contact.contactId}")
//                                }
                            }
                        } else {
                            Log.i("Permission: ", "Denied")
                            Toast.makeText(this,
                                "You need to add permission in order to access contacts",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    inner class ContactObserver(private val handler: Handler) : ContentObserver(handler) {

        private var isPendingUpdate = false
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            Log.d("Contact", "Contact changed on phone")
            if (!isPendingUpdate) {
                Log.d("Contact", "No pending updates")
                isPendingUpdate = true
                handler.postDelayed({
                    val cursor = contentResolver.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                    )
                    if (cursor == null) {
                        Log.d("Contact", "Contact cursor is null")
                        return@postDelayed
                    }
                    Log.d("Contact", "Syncing contact changes")
                    cursor.close()
                    isPendingUpdate = false
                }, 5000)
            }else {
                Log.d("Contact", "Pending updates")
            }
        }
    }

    @SuppressLint("Range")
    fun getAllContacts(): List<SQLiteContact> {
        // Getting all the contacts from the phone
        val contacts = mutableListOf<SQLiteContact>()
        val contentResolver = this.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
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
                val contactPhoneNumber = getPhoneNumber(contactId)

                val contactAccountName = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
                )

                val accountType = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)
                )

                val sync1 = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.RawContacts.SYNC1)
                )

                val sync2 = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.RawContacts.SYNC2)
                )

                val sync3 = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.RawContacts.SYNC3)
                )

                val sync4 = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.RawContacts.SYNC4)
                )

                Log.d("Contact", "Id: $contactId | Name: $contactName | Phone: $contactPhoneNumber | " +
                        "AccountName: $contactAccountName | Type: $accountType | Sync1: $sync1 | " +
                        "Sync2: $sync2 | Sync3: $sync3 | Sync4: $sync4")

//                contacts.add(SQLiteContact(
//                    contactPhoneNumber,
//                    contactName,
//                    "",
//                    "",
//                    true
//                ))
            }
        }
        cursor?.close()
        return contacts
    }

    @SuppressLint("Range")
    private fun getPhoneNumber(contactId: String): String {
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

    override fun onStart() {
        super.onStart()

        // First check for permissions
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i("Permission: ", "Granted")
                    chatViewModel.startListeningForChats(this)
                    chatViewModel.startListeningForContacts()
                } else {
                    Log.i("Permission: ", "Denied")
                    Toast.makeText(this,
                        "You need to add permission in order to access contacts",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

//        chatViewModel.startListeningForChats(this)
    }

    override fun onStop() {
        super.onStop()
        chatViewModel.stopListeningForChats()
        chatViewModel.stopListeningForContacts()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}