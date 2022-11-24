package com.example.firebasestoreandauth.dto

data class Comment(
    val profileImageRef: String,
    val uid: String,
    val comment: String
)