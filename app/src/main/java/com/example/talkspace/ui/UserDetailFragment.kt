package com.example.talkspace.ui

//import com.google.firebase.firestore.ktx.firestore
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.talkspace.MainActivity
import com.example.talkspace.R
import com.example.talkspace.SignInActivity
import com.example.talkspace.databinding.FragmentUserDetailBinding
import com.example.talkspace.repositories.LocalProfilePhotoStorage
import com.example.talkspace.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var binding: FragmentUserDetailBinding? = null

    private val currentUser = Firebase.auth.currentUser
    private var userName: String = ""
    private var userAbout: String = ""
    private var newPhotoUri = Uri.EMPTY

    private val userViewModel: UserViewModel by activityViewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
        if (it != null){
            Log.d("UserDetailFragment","Image picker : $it")
//        Glide.with(binding!!.userProfileImage.context).load(it).into(binding!!.userProfileImage)
            showCustomPhotoDialog(it)
            newPhotoUri = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = DataBindingUtil.inflate<FragmentUserDetailBinding>(
            inflater,
            R.layout.fragment_user_detail,
            container,
            false
        ).apply {
            viewModel = userViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        binding = fragmentBinding

        (requireActivity() as MainActivity).hideBottomNavigation()
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backBtn = getView()?.findViewById<ImageView>(R.id.back_btn)
        loadUserDetails()
//        loadUserProfilePhoto()

        binding?.editNameIcon?.setOnClickListener {
            showCustomDialog("Edit Name :", userName, "userName")
        }
        binding?.imageStorageButton?.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

//            updateProfilePhoto()
        }

        binding?.editAboutIcon?.setOnClickListener {
            showCustomDialog("Edit About :", userAbout, "userAbout")
        }

        backBtn?.setOnClickListener {
//            findNavController().navigate(R.id.action_userDetailFragment_to_mainFragment)
            findNavController().navigateUp()
        }

        binding?.signOutCard?.setOnClickListener {
            logout()
        }

//        binding?.deleteAccountText?.setOnClickListener { logout() }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun loadUserProfilePhoto(){
        Glide.with(binding!!.userProfileImage.context).load(R.drawable.ic_baseline_person_24).into(
            binding!!.userProfileImage)
        if (currentUser == null){
            logout()
            return
        }
        Log.d("UserDetailFragment"," Getting user Profile photo ")
        val image = LocalProfilePhotoStorage.getProfilePhotoFromLocalStorage(requireContext())
        if (image == null){
            val imageFile = File("${requireContext().cacheDir}/profile.png")
            Firebase.storage.reference.child("User profiles/${currentUser.uid}.png")
                .getFile(imageFile)
                .addOnSuccessListener {
                    Glide.with(binding?.userProfileImage!!.context).load(LocalProfilePhotoStorage.getProfilePhotoFromLocalStorage(requireContext())).into(binding!!.userProfileImage)
                }
                .addOnFailureListener{}
        }else{
            Log.d("UserDetailFragment","Got profile photo form cache ${image}")
            Glide.with(binding?.userProfileImage!!.context).load(image).into(binding!!.userProfileImage)
        }
    }

    private fun loadUserDetails() {
        // Load user name
        // TODO: Add data binding for user details
        Log.d("UserRepo", "Activity: ${requireActivity()}")
//        Log.d("UserRepo", "Loading user name: ${userViewModel.userName.value}")
//        binding?.displayName?.text = userViewModel.userName.value
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        LocalProfilePhotoStorage.clearCache(requireContext())
        startActivity(Intent(requireContext(), SignInActivity::class.java))
        activity?.finish()
    }

    private fun showCustomDialog(title: String, setInputDialog: String, key: String) {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle(title)
//            .setView(R.layout.custom_dialog)
//            .show()

        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("setEditInput", setInputDialog)
        bundle.putString("key", key)

        val customDialog = CustomDialog()
        customDialog.arguments = bundle
        customDialog.show(parentFragmentManager, "custom")
    }

    private fun showCustomPhotoDialog(photoUri: Uri){
        val bundle = Bundle()
        bundle.putString("photoUri",photoUri.toString())
        val customPhotoDialog = CustomPhotoDialog()
        customPhotoDialog.arguments = bundle
        customPhotoDialog.show(parentFragmentManager,"coustem")
    }
}
