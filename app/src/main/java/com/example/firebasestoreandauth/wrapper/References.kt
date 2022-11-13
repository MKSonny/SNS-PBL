package com.example.firebasestoreandauth.wrapper

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

fun getImageReference(fileName: String): StorageReference? {
    val storage = Firebase.storage
    val storageRef = storage.reference // reference to root
    if (fileName == "") return null
    return storage.getReferenceFromUrl(fileName)
}

fun getUserDocumentReference(uid: String): DocumentReference? {
    val firestore = Firebase.firestore
    if (uid.isEmpty()) return null
    val userCollection = firestore.collection("Users")
    return userCollection.document(uid)
}

/**
 * 자기 자신에 대한 문서 참조를 리턴하는 함수
 */
fun getReferenceOfMine(): DocumentReference? {
    val auth = Firebase.auth
    if (auth.currentUser != null)
        return auth.uid?.let { getUserDocumentReference(it) }
    return null
}
