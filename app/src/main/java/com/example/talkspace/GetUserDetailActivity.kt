package com.example.talkspace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.talkspace.databinding.ActivityGetUserDetailBinding
import com.example.talkspace.model.User
import com.example.talkspace.repositories.UserRepositories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class GetUserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGetUserDetailBinding

    private val db = Firebase.firestore

    private val currentUser = Firebase.auth.currentUser

    private lateinit var auth: FirebaseAuth

    private var newUri: Uri = Uri.EMPTY

    private lateinit var storageRef: StorageReference
    private val pickImage = registerForActivityResult(PickVisualMedia()) {
        if (it != null) {
            onImageSelected(uri = it)
            newUri = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageRef = Firebase.storage.reference
        binding.continueBtn.setOnClickListener {
            addUserInFireStore()
        }

        binding.imageStorageButton.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }

    private fun addUserInFireStore() {

        Log.d("GetUserDetailActivity", "user detail adding in fireStore")
        val addUser = User(
            currentUser?.uid,
            binding.userNameIpt.text.toString(),
            binding.userAboutIpt.text.toString(),
            currentUser?.phoneNumber,
            "",
            "Online"
        )
        if (currentUser != null) {
            if (!areDetailsInvalid()) {
                binding.progressBarContinueBtn.visibility = View.VISIBLE
                binding.continueBtn.visibility = View.INVISIBLE
                Log.d("GetUserDetailActivity", "Uploading user details")
                Log.d("GetUserDetailActivity", "Are details invalid: ${areDetailsInvalid()}")
                db.collection("users")
                    .document(currentUser.phoneNumber.toString())
                    .set(addUser)
                    .addOnSuccessListener {
                        Log.d("GetUserDetailActivity", "user detail add in fireStore")

                        if (newUri != Uri.EMPTY) {
                            saveUserProfilePhoto(newUri)  // save in local cache
                            uploadProfilePhoto()
                        }
                        val profileUpdate = userProfileChangeRequest {
                            displayName = binding.userNameIpt.text.toString()
                        }
                        currentUser.updateProfile(profileUpdate)
                            .addOnSuccessListener {
                                Log.d(
                                    "GetUserDetailActivity",
                                    "Updated user profile"
                                )
                            }
                            .addOnFailureListener {
                                Log.d(
                                    "GetUserDetailActivity",
                                    "user profile not updated"
                                )
                            }

                        binding.progressBarContinueBtn.visibility = View.GONE
                        binding.continueBtn.visibility = View.VISIBLE
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        binding.progressBarContinueBtn.visibility = View.GONE
                        binding.continueBtn.visibility = View.VISIBLE
                        Log.d("GetUserDetailActivity", "user detail not add in fireStore")
                    }
            } else {
                Toast.makeText(this, "Please enter a name and about", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //    upload profile photo in firebase storage and update firestore
    private fun uploadProfilePhoto() {
        storageRef.child("User profiles/${currentUser!!.uid}.png")
            .putFile(newUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { uri ->
                        Firebase.firestore.collection("users").document(currentUser.phoneNumber.toString())
                            .update("userPhotoUrl", uri)
                            .addOnSuccessListener {
                                Log.d("GetUserDetailActivity", "User photo upload")
                                val profileUpdate = userProfileChangeRequest { photoUri = uri }
                                currentUser.updateProfile(profileUpdate)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "GetUserDetailActivity",
                                            "Update user profile photo uri"
                                        )
                                    }
                            }
                            .addOnFailureListener {
                                Log.d(
                                    "GetUserDetailActivity",
                                    "User photo not upload"
                                )
                            }
                    }
            }
            .addOnFailureListener {
                Log.w(
                    "GetUserDetailActivity",
                    "Photo not store in firebase storage --> Task unsuccessful",
                    it
                )
            }
    }

    //    input check
    private fun areDetailsInvalid(): Boolean {
        val newUserName = binding.userNameIpt.text.toString().trim()
        val newUserAbout = binding.userAboutIpt.text.toString().trim()
        return newUserName == "" || newUserName == "null"
                || newUserAbout == "" || newUserAbout == "null"
    }

    //    show image in image view
    private fun onImageSelected(uri: Uri) {
        Glide.with(binding.userUploadedImage).load(uri).into(binding.userUploadedImage)
    }

    //    For Offline caching
    private fun saveUserProfilePhoto(uri: Uri) {
        UserRepositories.saveUserProfilePhoto(this, uri)
    }
}