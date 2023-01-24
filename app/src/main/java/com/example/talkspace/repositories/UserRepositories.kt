package com.example.talkspace.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

object UserRepositories {

//    bitmap conversion and caching function
    fun saveUserProfilePhoto(context : Context, uri : Uri){
        val bitmap : Bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        LocalProfilePhotoStorage.saveProfilePhotoToLocalStorage(context, bitmap)
    }
}