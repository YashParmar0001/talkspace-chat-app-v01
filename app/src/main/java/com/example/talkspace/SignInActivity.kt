package com.example.talkspace

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.talkspace.databinding.ActivitySignInBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.SimpleTimeZone
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private var phoneNumber=""
    private lateinit var auth: FirebaseAuth

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Log.d("SignInActivity", "On verification completed: $p0")
                binding.progressBarSendOtp.visibility = View.GONE
                binding.signBtn.visibility = View.VISIBLE
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("SignInActivity", "$e")
                binding.progressBarSendOtp.visibility = View.GONE
                binding.progressBarSendOtp.visibility = View.VISIBLE
            }

            override fun onCodeSent(verificationId: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, p1)
                Log.d("SignInActivity", verificationId)
                binding.progressBarSendOtp.visibility = View.VISIBLE
                binding.progressBarSendOtp.visibility = View.INVISIBLE

                val intent = Intent(applicationContext,SendOtpActivity::class.java)
                intent.putExtra("PhoneNumber",phoneNumber)
                intent.putExtra("VerificationId",verificationId)
                startActivity(intent)
                finish()
            }
        }

        binding.signBtn.setOnClickListener{
            getPhoneNumber()
        }

    }

    private fun getPhoneNumber(){
        phoneNumber = binding.numberIptEt.text.toString()
        if(phoneNumber.isNotEmpty() && phoneNumber.trim().length == 10){
            phoneNumber = "+91$phoneNumber"
            Log.d("SignInActivity",phoneNumber)
            sendVerificationCode(phoneNumber)
        }
    }

    private fun sendVerificationCode(number: String){
        binding.progressBarSendOtp.visibility = View.VISIBLE
        binding.signBtn.visibility = View.INVISIBLE
        val option = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        Log.d("SignInActivity","$option")
        PhoneAuthProvider.verifyPhoneNumber(option)
        Log.d("SignInActivity","sendOTP")
    }
}