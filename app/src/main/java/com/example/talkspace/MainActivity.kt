package com.example.talkspace

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.talkspace.databinding.ActivityMainBinding
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.observers.ContactsChangeObserver
import com.example.talkspace.ui.currentUser
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
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

    private lateinit var contactObserver: ContactsChangeObserver

    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var preferences: SharedPreferences
    private var isFirstTimeLogin: Boolean = false

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

            preferences = getSharedPreferences("contactPrefs", MODE_PRIVATE)
            isFirstTimeLogin = preferences.getBoolean("firstTime", true)

            // Todo: Set user state as "Online"

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
                // Registering contacts change observer
                lifecycleScope.launch(Dispatchers.IO) {
                    chatViewModel.syncContacts(firestore, contentResolver, isFirstTimeLogin)
                }
                changeFirstTime()
                chatViewModel.startListeningForChats()
                chatViewModel.startListeningForContacts()
                registerContactsChangeObserver()
            }else {
                val requestPermissionLauncher =
                    registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->
                        if (isGranted) {
                            Log.i("Permission: ", "Granted")
                            // Register contacts change observer
                            lifecycleScope.launch(Dispatchers.IO) {
                                chatViewModel.syncContacts(firestore, contentResolver, isFirstTimeLogin)
                            }
                            changeFirstTime()
                            chatViewModel.startListeningForChats()
                            chatViewModel.startListeningForContacts()
                            registerContactsChangeObserver()
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

    private fun registerContactsChangeObserver() {
        contactObserver = ContactsChangeObserver(
            Handler(Looper.getMainLooper()),
            lifecycleScope,
            contentResolver,
            chatViewModel
        )
        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            contactObserver
        )
    }

    override fun onStart() {
        super.onStart()

        chatViewModel.startListeningForContacts()
    }

    override fun onStop() {
        super.onStop()
//        chatViewModel.stopListeningForChats()
//        chatViewModel.stopListeningForContacts()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun changeFirstTime() {
        if (isFirstTimeLogin) {
            Log.d("NotifyContact", "First time sign in")
            val editor: SharedPreferences.Editor = preferences.edit()
            editor.putBoolean("firstTime", false)
            editor.apply()
        }else {
            Log.d("NotifyContact", "Not first time sign in")
        }
    }
}