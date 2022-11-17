package com.example.firebasestoreandauth

data class Comment(
    val profileImageRef: String,
    val uid: String,
    val comment: String
)