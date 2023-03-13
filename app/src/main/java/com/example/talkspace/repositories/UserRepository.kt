package com.example.talkspace.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.talkspace.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    fun getUserDataOffline(): User {
        return User(
            preferences.getString("user_id", null),
            preferences.getString("user_name", null),
            preferences.getString("user_about", null),
            preferences.getString("user_phone_no", null),
            preferences.getString("user_photo_url", null),
            preferences.getString("user_status", null)
        )
    }

    fun getUserDataOnline(): Task<DocumentSnapshot> {
        Log.d("UserRepo", "Getting user data online")
        return firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .get()
    }

    fun changeUserNameOnline(userName: String): Task<Void> {
        // First try to change on server
        return FirebaseFirestore.getInstance().collection("users")
            .document(currentUser?.phoneNumber.toString())
            .update("userName", userName)
    }

    fun changeUserNameOffline(userName: String) {
        val editor = preferences.edit()
        editor.putString("user_name", userName)
        editor.apply()
    }

    fun changeUserAboutOnline(userAbout: String): Task<Void> {
        // First try to change on server
        return FirebaseFirestore.getInstance().collection("users")
            .document(currentUser?.phoneNumber.toString())
            .update("userAbout", userAbout)
    }

    fun changeUserAboutOffline(userAbout: String) {
        val editor = preferences.edit()
        editor.putString("user_about", userAbout)
        editor.apply()
    }

    fun startSharedPrefListener(task: Runnable) {
        preferences.registerOnSharedPreferenceChangeListener { preferences, key ->
            Log.d("UserRepo", "Data changed: $key | ${preferences.getString(key, "")}")
            task.run()
        }
    }

    fun stopSharedPrefListener() {
        preferences.unregisterOnSharedPreferenceChangeListener { preferences, key ->
            Log.d("UserRepo", "Stopping listener")
        }
    }

    fun storeUserDataOffline(user: User) {
        Log.d("UserRepo", "Storing user data offline: ${user.userName} | ${user.userAbout}")
        val editor = preferences.edit()
        editor.putString("user_id", user.userId)
        editor.putString("user_name", user.userName)
        editor.putString("user_about", user.userAbout)
        editor.putString("user_phone_no", user.userPhoneNumber)
        editor.putString("user_photo_uri", user.userPhotoUrl)
        editor.putString("user_status", user.userStatus)
        editor.apply()
    }
}