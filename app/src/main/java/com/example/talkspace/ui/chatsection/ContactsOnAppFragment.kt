package com.example.talkspace.ui.chatsection

import android.Manifest
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.talkspace.ApplicationClass
import com.example.talkspace.R
import com.example.talkspace.databinding.FragmentContactsOnAppBinding
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

class ContactsOnAppFragment : Fragment() {

    private lateinit var binding: FragmentContactsOnAppBinding

    // Firebase instance variables
    private val firestore = FirebaseFirestore.getInstance()
    private val chatViewModel: ChatViewModel by activityViewModels {
        ChatViewModelFactory((activity?.application as ApplicationClass).repository)
    }

    private val pickContact = registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        Log.d("PickContact", "Contact: $uri")
        if (uri == null) {
            return@registerForActivityResult
        }

        val contentResolver = requireContext().contentResolver

        // For getting contact name
        val contactCursor = contentResolver.query(uri, null, null, null, null)
        val nameIndex = contactCursor?.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        contactCursor?.moveToFirst()
        val contactName = nameIndex?.let { contactCursor.getString(it) }
        Log.d("PickContact", "Contact name: $contactName")

        // For getting phone number
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(uri.lastPathSegment),
            null
        )
        var phoneNumber = ""
        if (phoneCursor != null && phoneCursor.moveToFirst()) {
            val index = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (index >= 0) phoneNumber = phoneCursor.getString(index)
        }

        // Process phone number
        phoneNumber = phoneNumber.replace(" ", "")
        Log.d("PickContact", "Phone number after trim: $phoneNumber")
        val length = phoneNumber.length
        if (length != 10) {
            phoneNumber = phoneNumber.substring(3, length)
        }
        Log.d("PickContact", "Contact number: $phoneNumber")
        contactCursor?.close()

        // Checking if contact is present in Firebase
        checkUserAndGoToChat("+91$phoneNumber", contactName.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsOnAppBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: For permissions
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i("Permission: ", "Granted")
                } else {
                    Log.i("Permission: ", "Denied")
                }
            }

        requestPermissionLauncher.launch(
            Manifest.permission.READ_CONTACTS
        )
        binding.addPhoneContact.setOnClickListener {
            pickContact.launch(null)
        }
    }

    private fun checkUserAndGoToChat(friendId: String, friendName: String) {
        firestore.collection("users")
            .document(friendId)
            .get().addOnSuccessListener {
                val value = it.get("userId")
                if (value == null) {
                    Toast.makeText(
                        requireContext(),
                        "User does not use the app",
                        Toast.LENGTH_LONG
                    ).show()
                }else {
                    Toast.makeText(
                        requireContext(),
                        "User uses the app",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.d("ContactFragment", "friendId: $friendId")
                    Log.d("ContactFragment", "friendName: $friendName")
                    chatViewModel.setCurrentFriendId(friendId)
                    chatViewModel.setCurrentFriendName(friendName)
                    findNavController().navigate(R.id.action_contactsOnApp_to_chatFragment)
                }
                return@addOnSuccessListener
            }
    }


}