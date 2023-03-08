package com.example.talkspace

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.talkspace.databinding.ActivityMainBinding
import com.example.talkspace.observers.ContactsChangeObserver
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.example.talkspace.viewmodels.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val chatViewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            (this.application as ApplicationClass).chatRepository,
            (this.application as ApplicationClass).contactRepository
        )
    }

    private val userViewModel: UserViewModel by viewModels()

    private lateinit var contactObserver: ContactsChangeObserver

    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var preferences: SharedPreferences
    private var isFirstTimeLogin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        } else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // TODO: For bottom navigation
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController
            setupSmoothBottomMenu()

            // TODO: For offline data storing
            preferences = getSharedPreferences("contactPrefs", MODE_PRIVATE)
            isFirstTimeLogin = preferences.getBoolean("firstTime", true)

            userViewModel.getUserDetails()

//            // Todo: Set user state as "Online"
//            chatViewModel.notifyUserState("Using app", this)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                // Registering contacts change observer
                lifecycleScope.launch(Dispatchers.IO) {
                    chatViewModel.syncContacts(firestore, contentResolver, isFirstTimeLogin)
                }
                changeFirstTime()
                chatViewModel.startListeningForChats()
                chatViewModel.startListeningForContacts()
                registerContactsChangeObserver()
            } else {
                val requestPermissionLauncher =
                    registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->
                        if (isGranted) {
                            Log.i("Permission: ", "Granted")
                            // Register contacts change observer
                            lifecycleScope.launch(Dispatchers.IO) {
                                chatViewModel.syncContacts(
                                    firestore,
                                    contentResolver,
                                    isFirstTimeLogin
                                )
                            }
                            changeFirstTime()
                            chatViewModel.startListeningForChats()
                            chatViewModel.startListeningForContacts()
                            registerContactsChangeObserver()
                        } else {
                            Log.i("Permission: ", "Denied")
                            Toast.makeText(
                                this,
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

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        chatViewModel.notifyUserState1(this)
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
        } else {
            Log.d("NotifyContact", "Not first time sign in")
        }
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.bottom_navigation_menu)
        val menu = popupMenu.menu
        binding.bottomNav.setupWithNavController(menu, navController)
    }

    fun hideBottomNavigation() {
        binding.bottomNav.visibility = View.GONE
    }

    fun showBottomNavigation() {
        binding.bottomNav.visibility = View.VISIBLE
    }
}