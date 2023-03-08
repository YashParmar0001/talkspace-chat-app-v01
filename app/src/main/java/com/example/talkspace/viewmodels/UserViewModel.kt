package com.example.talkspace.viewmodels

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.talkspace.model.User
import com.example.talkspace.repositories.UserRepository
import com.example.talkspace.ui.currentUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {
    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData

//    private val _userName = MutableLiveData<String>(userData.value?.userName)
//    val userName: LiveData<String> = userData.value.userName
//
//    private val _userAbout = MutableLiveData<String>(userData.value?.userAbout)
//    val userAbout: LiveData<String> = _userAbout

    // TODO: Photo feature will be implemented later
//    private val _userPhotoUri = MutableLiveData<String>()
//    val userPhotoUri: LiveData<String> = _userPhotoUri

//    private val _userPhoneNumber = Firebase.auth.currentUser.toString()

//    fun setUserName(name: String) {
//        _userName.value = name
//    }

//    fun setUserAbout(userAbout: String) {
//        _userAbout.value = userAbout
//    }

    fun getUserDetails() {
        // First try to get data offline
        _userData.value = userRepository.getUserDataOffline()
        Log.d("UserRepo", "Got offline data: " +
                "${userData.value?.userName} | ${userData.value?.userAbout}")
        // Now get data online from firebase
        userRepository.getUserDataOnline().addOnSuccessListener { snapshot ->
            Log.d("UserRepo", "Got online user data")
            if (snapshot != null) {
                val onlineUser = User(
                    snapshot["userId"].toString(),
                    snapshot["userName"].toString(),
                    snapshot["userAbout"].toString(),
                    snapshot["userPhoneNumber"].toString(),
                    snapshot["userPhotoUrl"].toString(),
                    snapshot["userStatus"].toString()
                )
                _userData.value = onlineUser
                Log.d("UserRepo", "Online data: ${userData.value?.userName}")
                userRepository.storeUserDataOffline(onlineUser)
            }else {
                Log.d("UserRepo", "User snapshot is null")
            }
        }
    }

    fun changeUserName(userName: String) {
        userRepository.changeUserName(userName)
    }

    fun changeUserAbout(userAbout: String) {

    }

    fun startListeningForUser() {
        userRepository.startSharedPrefListener()
    }

    fun stopListeningForUser() {
        userRepository.stopSharedPrefListener()
    }

    fun printUserName() {
        Log.d("UserRepo", "User name: ${userData.value?.userName}")
    }
}