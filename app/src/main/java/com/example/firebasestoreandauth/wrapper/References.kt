package com.android.pblsns.firebase.wrapper

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
