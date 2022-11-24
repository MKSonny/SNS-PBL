package com.example.firebasestoreandauth.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel

class ProfileViewModel() : ViewModel() {
    var postImgUrl: Uri? = null
    var profileUrl: String? = null
    var imgFile: Long? = null
    var imgName: String? = null
    var bitmap: Bitmap? = null

    fun setPos(ImgUrl: Uri?) {
        postImgUrl = ImgUrl
    }

    fun getPos(): Uri? {
        return postImgUrl
    }

    fun setPro(proUrl: String?) {
        profileUrl = proUrl
    }

    fun getPro(): String? {
        return profileUrl
    }

    fun setFile(proUrl: Long?) {
        imgFile = proUrl
    }

    fun getFile(): Long? {
        return imgFile
    }

    fun setName(proUrl: String?) {
        imgName = proUrl
    }

    fun getName(): String? {
        return imgName
    }

    fun setbit(proUrl: Bitmap) {
        bitmap = proUrl
    }

    fun getbit(): Bitmap? {
        return bitmap
    }
}