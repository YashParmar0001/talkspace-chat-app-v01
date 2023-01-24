package com.example.talkspace.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object LocalProfilePhotoStorage {

//    save Profile Photo to Local Storage (path Diff.)and compress bitmap
    fun saveProfilePhotoToLocalStorage(context: Context,finalBitmap : Bitmap){
        val imageFile = File("${context.cacheDir}/profile.png")
        try {
            val out = FileOutputStream(imageFile)
            finalBitmap.compress(Bitmap.CompressFormat.PNG,100, out)
            out.flush()
            out.close()
            Log.d("LocalProPhotoStorage","Photo Save To local Storage : ${imageFile}",)
        }catch (e: Exception){
            Log.w("LocalProPhotoStorage","Photo is not save: ",e)
        }
    }

    fun clearCache(context: Context){
        val cacheFile = File("${context.cacheDir}/profile.png")
        if (cacheFile.exists()){
            cacheFile.delete()
        }
        Log.d("LocalProfilePhotoStorage","clear Cache !!!")
    }

    fun getProfilePhotoFromLocalStorage(context: Context) : Bitmap?{
        try {
            Log.d("LocalProPhotoStorage","Loading profile photo from local Storage ...")
            val uri = Uri.parse("file://${context.cacheDir}/profile.png")
            return BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        }catch (e : java.lang.Exception){
            Log.d("LocalProPhotoStorage","unable to load user profile")
        }
        return null
    }
}