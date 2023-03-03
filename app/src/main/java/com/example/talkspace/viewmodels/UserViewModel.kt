package com.example.talkspace.viewmodels

import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.talkspace.repositories.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val userRepository: UserRepository): ViewModel() {
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userAbout = MutableLiveData<String>()
    val userAbout: LiveData<String> = _userAbout

    private val _userPhotoUri = MutableLiveData<String>()
    val userPhotoUri: LiveData<String> = _userPhotoUri

    private val _userPhoneNumber = Firebase.auth.currentUser.toString()

    fun setUserName(name: String) {
        _userName.value = name
        Log.d("UserRepo", "User name set: ${userName.value}")
        userRepository.storeUserName(name)
    }

    fun setUserAbout(userAbout: String) {
        _userAbout.value = userAbout
    }

    fun getUserDetails() {
        Log.d("UserRepo", "Getting user details...")
        val name = userRepository.getUserNameOffline()
        if (name == null) {
            userRepository.getUserDataOnline().addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    val userName = snapshot["userName"].toString()
                    setUserName(userName)
                }else {
                    Log.d("UserRepo", "Snapshot is null")
                }
            }
        }else {
            Log.d("UserRepo", "User name found offline: $name")
            setUserName(name)
        }

    }
}

class UserViewModelFactory(private val userRepository: UserRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}