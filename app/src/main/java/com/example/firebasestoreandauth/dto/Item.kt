package com.example.firebasestoreandauth.dto

import com.google.firebase.Timestamp

data class Item(
    val profile_img: String,
    val postId: String,
    val postImgUrl: String,
    var likes: Number,
    val time: Timestamp,
    val whoPosted: String,
    var comments: ArrayList<Map<String, String>>,
    var liked: Boolean = false
)