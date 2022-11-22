package com.example.firebasestoreandauth.DTO

data class Item(
    val profile_img: String ?= "gs://sns-pbl.appspot.com/post_img_err.png",
    val postId: String ?= User.INVALID_USER,
    val postImgUrl: String ?= "gs://sns-pbl.appspot.com/post_img_err.png",
    var likes: Number ?= 0,
    //val time: Timestamp,
    val whoPosted: String ?= User.INVALID_USER,
    var comments: ArrayList<Map<String, String>>
)