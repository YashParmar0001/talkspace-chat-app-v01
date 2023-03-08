package com.example.talkspace.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.talkspace.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CustomDialog  : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getDialog()!!.getWindow()!!.setBackgroundDrawableResource(R.drawable.custom_dialog_contener)
        val view = inflater.inflate(R.layout.custom_dialog, container, false)
        view.findViewById<TextView>(R.id.dialog_title).text = arguments?.getString("title")
        view.findViewById<EditText>(R.id.input_dialog).setText(arguments?.getString("setEditInput"))
        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.89).toInt()
//        val height = (resources.displayMetrics.heightPixels * 0.30).toInt()
        getDialog()!!.getWindow()!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val saveButton = view.findViewById<TextView>(R.id.save_btn)
        val cancel = view.findViewById<TextView>(R.id.cancel_btn)
        val inputText = view.findViewById<EditText>(R.id.input_dialog)
        val key = arguments?.getString("key").toString()

        saveButton.setOnClickListener {
            val updateText = inputText.text.toString().trim()
            if (!areDetailsIsInvalid(updateText)) {
                updateDataBase(key, updateText)
                dismiss()
            } else {
                Toast.makeText(requireContext(),"Enter detail is empty",Toast.LENGTH_LONG).show()
            }
        }
        cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun updateDataBase(key: String, updateText: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.currentUser?.phoneNumber.toString())
            .update(key, updateText)
            .addOnSuccessListener { Log.d("Update ", "update user detail $key") }
            .addOnFailureListener { Log.d("Update", "update is fail") }
    }

    private fun areDetailsIsInvalid(inputText: String): Boolean {
        return inputText == "" || inputText == "null"
    }

    fun changeUserName() {

    }
}