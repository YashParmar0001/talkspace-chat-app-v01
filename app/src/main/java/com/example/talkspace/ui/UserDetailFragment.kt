package com.example.talkspace.ui

//import com.google.firebase.firestore.ktx.firestore
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.example.talkspace.databinding.EditAboutDialogBinding
import com.example.talkspace.databinding.EditNameDialogBinding
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

    private lateinit var editNameDialog: Dialog
    private lateinit var editAboutDialog: Dialog

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
            userDetailsFragment = this@UserDetailFragment
            lifecycleOwner = viewLifecycleOwner
        }
        binding = fragmentBinding

        (requireActivity() as MainActivity).hideBottomNavigation()
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backBtn = getView()?.findViewById<ImageView>(R.id.back_btn)

        // TODO: For edit dialogs
        val editNameDialogBinding = EditNameDialogBinding.inflate(layoutInflater)
        editNameDialog = Dialog(requireContext())
        editNameDialog.setContentView(editNameDialogBinding.root)
//        editAboutDialog.window?.setBackgroundDrawableResource()

        editNameDialogBinding.saveUsernameBtn.setOnClickListener {
            Log.d("UserRepo", "Changing username")
            editNameDialog.dismiss()
            val progressBar = binding?.usernameProBar
            progressBar?.visibility = View.VISIBLE
            val newUserName = editNameDialogBinding.editUsernameInput.text.toString()
            userViewModel.changeUserNameOnline(newUserName)
                .addOnSuccessListener {
                    userViewModel.changeUserNameOffline(newUserName)
                    progressBar?.visibility = View.GONE
                }.addOnFailureListener {
                    Log.d("UserRepo", "Failed to change username", it)
                }
        }

        editNameDialogBinding.cancelNameBtn.setOnClickListener {
            editNameDialog.dismiss()
        }

        val editAboutDialogBinding = EditAboutDialogBinding.inflate(layoutInflater)
        editAboutDialog = Dialog(requireContext())
        editAboutDialog.setContentView(editAboutDialogBinding.root)

        editAboutDialogBinding.saveAboutBtn.setOnClickListener {
            Log.d("UserRepo", "Changing userabout")
            editAboutDialog.dismiss()
            val progressBar = binding?.useraboutProBar
            progressBar?.visibility = View.VISIBLE
            val newUserAbout = editAboutDialogBinding.editAboutInput.text.toString()
            userViewModel.changeUserAboutOnline(newUserAbout)
                .addOnSuccessListener {
                    userViewModel.changeUserAboutOffline(newUserAbout)
                    progressBar?.visibility = View.GONE
                }.addOnFailureListener {
                    Log.d("UserRepo", "Failed to change userabout", it)
                }
        }

        editAboutDialogBinding.cancelAboutBtn.setOnClickListener {
            editAboutDialog.dismiss()
        }

        backBtn?.setOnClickListener {
            navigateBack()
        }
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

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        LocalProfilePhotoStorage.clearCache(requireContext())
        startActivity(Intent(requireContext(), SignInActivity::class.java))
        activity?.finish()
    }

    fun navigateBack() {
        findNavController().navigateUp()
    }

    fun showEditNameDialog() {
        editNameDialog.show()
    }

    fun showEditAboutDialog() {
        editAboutDialog.show()
    }

    private fun showCustomPhotoDialog(photoUri: Uri){
        val bundle = Bundle()
        bundle.putString("photoUri",photoUri.toString())
        val customPhotoDialog = CustomPhotoDialog()
        customPhotoDialog.arguments = bundle
        customPhotoDialog.show(parentFragmentManager,"coustem")
    }
}
