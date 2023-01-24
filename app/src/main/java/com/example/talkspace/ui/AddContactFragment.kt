package com.example.talkspace.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.talkspace.R
import com.example.talkspace.databinding.FragmentAddContactBinding

const val READ_CONTACTS_PERMISSION_REQUEST = 1
class AddContactFragment : Fragment() {

    private var binding : FragmentAddContactBinding? = null

    // For photo selection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentAddContactBinding.inflate(inflater,container,false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.addContactBtn?.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_contactsOnApp)
//            pickContact.launch(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}