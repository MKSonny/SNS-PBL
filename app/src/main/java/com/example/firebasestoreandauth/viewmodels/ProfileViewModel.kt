package com.example.firebasestoreandauth.wrapper

import android.net.Uri
import androidx.lifecycle.ViewModel

class ProfileViewModel() : ViewModel() {
    var postImgUrl: Uri? = null
    var profileUrl: String? = null

    fun setPos(ImgUrl: Uri?){
        postImgUrl = ImgUrl
    }

    fun getPos() : Uri?{
        return postImgUrl
    }

    fun setPro(proUrl: String?){
        profileUrl = proUrl
    }

    fun getPro() : String?{
        return profileUrl
    }
}