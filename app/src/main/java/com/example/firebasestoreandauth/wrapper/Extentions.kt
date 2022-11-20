@file:Suppress("UNCHECKED_CAST")

package com.example.firebasestoreandauth.wrapper

import android.util.Log
import com.example.firebasestoreandauth.DTO.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.ktx.Firebase

/**
 * 사용자를 찾는 DocumentSnapshot 에서
 * User를 추출하는 확장메서드
 */
fun DocumentSnapshot.toUser(): User {
    return this.data.let {
        User(
            uid = it?.get("uid").toString() ?: User.INVALID_USER,
            nickname = it?.get("nickname").toString() ?: User.INVALID_USER,
            BirthDay = it?.get("birthDay").toString() ?: User.INVALID_USER,
            profileImage = it?.get("profileImage").toString() ?: User.INVALID_USER,
            friends = (it?.get("friends") ?: listOf<String>()) as List<String>,
            requestSent = (it?.get("requestSent") ?: listOf<String>()) as List<String>,
            requestReceived = (it?.get("requestReceived") ?: listOf<String>()) as List<String>,
        )
    }

}

/**
 *  친구요청을 보내는 Extension Function
 */
fun DocumentReference.sendRequestToFriend() {
    val reference = this
    val cu = Firebase.auth.currentUser
    val uid = cu?.uid ?: ""
    if (uid.isNotEmpty()) {
        this.get().addOnSuccessListener { snapshot ->
            val other = snapshot.toUser()
            if (other.uid == User.INVALID_USER) {

            }
        }
    }
}

/**
 *  친구요청을 삭제하는 Extension Function
 *  @param uid 삭제하려는 대상
 */
fun DocumentReference.removeReceivedRequest(uid: String) {
    this.get().addOnCompleteListener { response ->
        run {
            val data = response.result.data
            val requestReceived = data?.get("requestReceived")?.run {
                this as MutableList<String>?
            }
            if (requestReceived != null) {
                for (item in requestReceived) {
                    if (item == uid) {
                        requestReceived.remove(uid)
                    }
                }
                this.update(mapOf("requestReceived" to requestReceived))
            }
        }
    }
}

fun DocumentReference.acceptFriendRequest(uid: String) {
    this.get().addOnCompleteListener { response ->
        run {
            val data = response.result.data
            val requestReceived =
                (data?.get("requestReceived") ?: listOf<String>()) as MutableList<String>
            val currentFriend = (data?.get("friends") ?: listOf<String>()) as MutableList<String>

            requestReceived?.let { requests ->
                val newRequest = requests.filter {
                    it != uid
                }
                this.update(mapOf("requestReceived" to newRequest))
            }
            currentFriend?.let { friends ->
                val res = friends.filter {
                    it == uid
                }
                if (res.isEmpty()) {
                    friends.add(uid)
                    this.update(mapOf("friends" to friends))
                }
            }
            //welcome to callback hell
            getUserDocumentWith(uid)?.let {
                get().addOnCompleteListener { snapshot ->
                    if (snapshot.isSuccessful) {
                        val other = snapshot.result.toUser()
                        val me = Firebase.auth.currentUser?.uid ?: ""
                        if (me.isEmpty())
                            return@addOnCompleteListener
                        val friends = (other.friends ?: listOf()).toMutableList()
                        if (!friends.contains(me))
                            friends.add(me)
                        val sent = (other.requestSent ?: listOf()).filter {
                            it != me
                        }
                        it.update(mapOf("friends" to friends))
                        it.update(mapOf("requestSent" to sent))
                    }
                }
            }
        }
    }
}

fun FirebaseAuth.signOut() {
    Log.d("FirebaseAuth", "${this.currentUser?.displayName}이/가 로그아웃 했습니다.")
    this.signOut()
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
