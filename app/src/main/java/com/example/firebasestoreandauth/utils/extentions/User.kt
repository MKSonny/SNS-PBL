package com.example.firebasestoreandauth.utils.extentions

import com.example.firebasestoreandauth.dto.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

fun DocumentSnapshot.toUser(): User {
    return this.data.let {
        User(
            uid = (it?.get("uid").toString() ?: User.INVALID_USER) as String,
            nickname = (it?.get("nickname").toString() ?: User.INVALID_USER) as String,
            BirthDay = (it?.get("birthDay").toString() ?: User.INVALID_USER) as String,
            profileImage = (it?.get("profileImage").toString() ?: User.INVALID_USER) as String,
            friends = (it?.get("friends") ?: listOf<String>()) as List<String>,
            requestSent = (it?.get("requestSent") ?: listOf<String>()) as List<String>,
            requestReceived = (it?.get("requestReceived") ?: listOf<String>()) as List<String>,
        )
    }
}

fun User.toFirebase(callback: () -> Unit?) {
    val firestore = Firebase.firestore
    val userCollection = firestore.collection("Users")
    userCollection.document(this.uid.toString()).set(this).addOnSuccessListener {
        callback()
    }
}

fun User.toFirebase() {
    val firestore = Firebase.firestore
    val userCollection = firestore.collection("Users")
    userCollection.document(this.uid.toString()).set(this)
}