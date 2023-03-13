package com.example.talkspace.viewmodels

import android.app.Dialog
import android.util.Log
import androidx.lifecycle.*
import com.example.talkspace.model.User
import com.example.talkspace.repositories.UserRepository
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {
    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData

    private val _userName = MutableLiveData<String>(userData.value?.userName)
    val userName: LiveData<String> = _userName

    private val _userAbout = MutableLiveData<String>(userData.value?.userAbout)
    val userAbout: LiveData<String> = _userAbout

    fun getUserDetails() {
        // First try to get data offline
        val offlineData = userRepository.getUserDataOffline()
        _userData.value = offlineData
        _userName.value = offlineData.userName.toString()
        _userAbout.value = offlineData.userAbout.toString()
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
                // TODO: For testing
                _userName.value = onlineUser.userName.toString()
                _userAbout.value = onlineUser.userAbout.toString()
                Log.d("UserRepo", "Online data: ${userData.value?.userName}")
                userRepository.storeUserDataOffline(onlineUser)
            }else {
                Log.d("UserRepo", "User snapshot is null")
            }
        }
    }

    fun changeUserNameOnline(userName: String): Task<Void> {
        return userRepository.changeUserNameOnline(userName)
    }

    fun changeUserNameOffline(userName: String) {
        userRepository.changeUserNameOffline(userName)
        _userData.value?.userName = userName
        _userName.value = userName
    }

    fun changeUserAboutOnline(userAbout: String): Task<Void> {
        return userRepository.changeUserAboutOnline(userAbout)
    }

    fun changeUserAboutOffline(userAbout: String) {
        userRepository.changeUserAboutOffline(userAbout)
        _userData.value?.userAbout = userAbout
        _userAbout.value = userAbout
    }

    fun startListeningForUser() {
        userRepository.startSharedPrefListener {
            val newData = userRepository.getUserDataOffline()
            if (newData.userName == null || newData.userAbout == null) {
                getUserDetails()
            }else {
                _userData.value = newData
                _userName.value = newData.userName.toString()
                _userAbout.value = newData.userAbout.toString()
            }
        }
    }

    fun stopListeningForUser() {
        userRepository.stopSharedPrefListener()
    }
}