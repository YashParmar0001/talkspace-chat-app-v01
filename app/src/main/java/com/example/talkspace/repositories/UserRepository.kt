package com.example.talkspace.repositories

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class UserRepository(private val context: Context) {
    val preferences = context.getSharedPreferences("user_prefs", 0)
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    fun getUserNameOffline(): String? {
        return preferences.getString("user_name", null)
    }

    fun storeUserName(userName: String) {
        val editor = preferences.edit()
        editor.putString("user_name", userName)
        editor.apply()
        Log.d("UserRepo", "User name stored: $userName")
    }

    fun getUserDataOnline(): Task<DocumentSnapshot> {
        return firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .get()
    }

    fun startListeningForUserDetails() {
        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d("UserRepo", "Error listening to user")
                    return@addSnapshotListener
                }

                if (snapshot != null) {

                }else {
                    Log.d("UserRepo", "User snapshot is null")
                }
            }
    }
}