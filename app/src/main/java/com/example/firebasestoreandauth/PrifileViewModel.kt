package com.example.firebasestoreandauth.wrapper

import android.net.Uri

class ProfileViewModel {
    var postImgUrl: Uri? = null

    fun setPos(ImgUrl: Uri?){
        postImgUrl = ImgUrl
    }

    fun getPos() : Uri?{
        return postImgUrl
    }
}