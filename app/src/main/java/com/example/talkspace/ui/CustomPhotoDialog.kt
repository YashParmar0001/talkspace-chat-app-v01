package com.example.talkspace.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.talkspace.R
import com.example.talkspace.repositories.UserRepositories
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

val storageRef : StorageReference = Firebase.storage.reference
val currentUser = Firebase.auth.currentUser
private var photoUrl: String = currentUser?.photoUrl.toString()
private lateinit var newPhotoUri : Uri

class CustomPhotoDialog(): DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getDialog()!!.getWindow()!!.setBackgroundDrawableResource(R.drawable.custom_dialog_contener)
        val view = inflater.inflate(R.layout.custom_photo_dialog,container,false)
        newPhotoUri = arguments?.getString("photoUri").toString().toUri()
        view.findViewById<ImageView>(R.id.set_image).setImageURI(newPhotoUri)
        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels*0.89).toInt()
        val height = (resources.displayMetrics.heightPixels*0.60).toInt()
        getDialog()!!.getWindow()!!.setLayout(width,height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val saveButton = view.findViewById<TextView>(R.id.save_btn)
        val cancelButton = view.findViewById<TextView>(R.id.cancel_btn)
        saveButton.setOnClickListener{
            updateProfilePhoto()
            dismiss()
        }
        cancelButton.setOnClickListener{dismiss()}

    }
    private fun updateProfilePhoto() {
        if (newPhotoUri != Uri.EMPTY) {
            UserRepositories.saveUserProfilePhoto(requireContext(), newPhotoUri)
            if (photoUrl == ""
                || photoUrl == "null"
            ) {
                storageRef.child("User profiles/${currentUser!!.phoneNumber}.png")
                    .putFile(newPhotoUri)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.metadata?.reference?.downloadUrl
                            ?.addOnSuccessListener { uri ->
                                Firebase.firestore.collection("users")
                                    .document(currentUser.phoneNumber.toString())
                                    .update("userPhotoUrl", uri)
                                    .addOnSuccessListener {
                                        Log.d("UserDetailFragment", "user photo upload")
                                        val profileUpdate = userProfileChangeRequest {
                                            photoUri = uri
                                        }
                                        currentUser.updateProfile(profileUpdate)
                                            .addOnSuccessListener {
                                                Log.d("UserDetailFragment", "Update user photo url")
                                            }
//                                        loadUserProfilePhoto()
                                    }
                                    .addOnFailureListener {
                                        Log.d("UserDetailFragment", "user photo not upload")
                                    }
                            }
                    }
                    .addOnFailureListener {
                        Log.d("UserDetailFragment", "Task unsuccessful", it)
                    }
            } else {
                storageRef.child("User profiles/${currentUser?.phoneNumber}.png")
                    .putFile(newPhotoUri)
                    .addOnSuccessListener {
                        Log.d("UserDetailFragment", "Photo changed on cloud storage")
//                        loadUserProfilePhoto()
                    }
                    .addOnFailureListener {
                        Log.d("UserDetailFragment", "Photo changed on not cloud storage", it)
                    }
            }
        }
    }
}