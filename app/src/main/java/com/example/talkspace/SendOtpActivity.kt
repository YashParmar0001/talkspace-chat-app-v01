package com.example.talkspace

import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.talkspace.databinding.ActivitySendOtpBinding
import com.example.talkspace.databinding.ActivitySignInBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class SendOtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendOtpBinding

    private lateinit var _otpNumber1: EditText
    private val otpNumber1  get()=_otpNumber1
    private lateinit var _otpNumber2: EditText
    private val otpNumber2 get() = _otpNumber2
    private lateinit var _otpNumber3 : EditText
    private val otpNumber3 get() = _otpNumber3
    private lateinit var _otpNumber4 : EditText
    private val otpNumber4 get() = _otpNumber4
    private lateinit var _otpNumber5 : EditText
    private val otpNumber5 get() = _otpNumber5
    private lateinit var _otpNumber6 : EditText
    private val otpNumber6 get() = _otpNumber6

    private lateinit var _otpCode : String
    private val otpCode get() = _otpCode

    private lateinit var _verificationId : String
    private val verificationId get() = _verificationId
    private lateinit var auth : FirebaseAuth

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        _otpNumber1 = binding.otpCode1
        _otpNumber2 = binding.otpCode2
        _otpNumber3 = binding.otpCode3
        _otpNumber4 = binding.otpCode4
        _otpNumber5 = binding.otpCode5
        _otpNumber6 = binding.otpCode6

        auth = FirebaseAuth.getInstance()
        _verificationId = intent.getStringExtra("VerificationId").toString()

        setUpOtpInputs()

        binding.verifyBtn.setOnClickListener{
            isCodeVelid()
            Log.d("SendOtpActivity","Resend send OTP verify : $verificationId")
            if(verificationId.isNotEmpty()){
                binding.progressBarVerifyOtp.visibility = View.VISIBLE
                binding.verifyBtn.visibility = View.INVISIBLE
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    verificationId,
                    otpCode)
                signInWithPhoneAuthCredential(credential)
            }
        }

        val phoneNumber = intent.getStringExtra("PhoneNumber").toString()
        binding.resendOtpTextBtn.setOnClickListener {
            resendOTP(phoneNumber)
        }
    }

    private fun isCodeVelid(){
        if(otpNumber1.text.toString().trim { it <= ' '}.isNotEmpty()
            && otpNumber2.text.toString().trim { it <= ' ' }.isNotEmpty()
            && otpNumber3.text.toString().trim { it <= ' ' }.isNotEmpty()
            && otpNumber4.text.toString().trim { it <= ' ' }.isNotEmpty()
            && otpNumber5.text.toString().trim { it <= ' ' }.isNotEmpty()
            && otpNumber6.text.toString().trim { it <= ' ' }.isNotEmpty()){

            _otpCode = otpNumber1.text.toString()+
                    otpNumber2.text.toString()+
                    otpNumber3.text.toString()+
                    otpNumber4.text.toString()+
                    otpNumber5.text.toString()+
                    otpNumber6.text.toString()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    binding.progressBarVerifyOtp.visibility = View.GONE
                    binding.verifyBtn.visibility = View.VISIBLE
                    Log.d("SendOtpActivity","singed In : $credential")

                    Log.d("SendOtpActivity"," Chack Details ")
                    val userName = FirebaseAuth.getInstance().currentUser!!.displayName
                    val isDetailsNotAvailable = userName == null || userName == "" || userName == "null"
                    if(isDetailsNotAvailable){
                        Log.d("SendOtpActivity","Details is not available: ${userName}")
                        startActivity(Intent(this, GetUserDetailActivity::class.java))
                        finish()
                    }else{
                        Log.d("SendOtpActivity", "Details is available")
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    }
                } else {
                    binding.progressBarVerifyOtp.visibility = View.GONE
                    binding.verifyBtn.visibility = View.VISIBLE
                    if (task.exception is FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(this,"Invalid OTP",Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun resendOTP(number: String){
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Log.d("SendOtpActivity", "On verification completed: $p0")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("SendOtpActivity", "Resend verification code : $e")
            }

            override fun onCodeSent(newVerificationId: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(newVerificationId, p1)
                Log.d("SendOtpActivity", "Resend verification code : $newVerificationId")
                Toast.makeText(applicationContext, "Resend OTP",Toast.LENGTH_SHORT).show()
                _verificationId = newVerificationId
            }
        }

        val option = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(option)
        Log.d("SendOtpActivity","sendOTP")
    }

    private fun setUpOtpInputs(){
        otpNumber1.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim{it <= ' '}.isNotEmpty()) {
                    otpNumber2.requestFocus()
                    if(binding.verifyBtn.isEnabled == false){
                        binding.verifyBtn.isEnabled = true
                    }
                }else{
                    binding.verifyBtn.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        otpNumber2.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim{it <= ' '}.isNotEmpty()){
                    otpNumber3.requestFocus()
                    if(binding.verifyBtn.isEnabled == false){
                        binding.verifyBtn.isEnabled = true
                    }
                }else{
                    binding.verifyBtn.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        otpNumber3.addTextChangedListener ( object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim{it <= ' '}.isNotEmpty()){
                    otpNumber4.requestFocus()
                    if(binding.verifyBtn.isEnabled == false){
                        binding.verifyBtn.isEnabled = true
                    }
                }else{
                    binding.verifyBtn.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        otpNumber4.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim{it <= ' '}.isNotEmpty()){
                    otpNumber5.requestFocus()
                    if(binding.verifyBtn.isEnabled == false){
                        binding.verifyBtn.isEnabled = true
                    }
                }else{
                    binding.verifyBtn.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        otpNumber5.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim{it <= ' '}.isNotEmpty()){
                    otpNumber6.requestFocus()
                    if(binding.verifyBtn.isEnabled == false){
                        binding.verifyBtn.isEnabled = true
                    }
                }else{
                    binding.verifyBtn.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

}