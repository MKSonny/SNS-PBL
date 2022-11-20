@file:Suppress("UNCHECKED_CAST")

package com.example.firebasestoreandauth.wrapper

import android.util.Log
import com.example.firebasestoreandauth.DTO.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
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
    val cu = Firebase.auth.currentUser
    val uid = cu?.uid ?: ""//현재 사용자의 uid를 가져옴
    if (uid.isNotEmpty()) {
        this.apply {
            val othDoc = this
            get().addOnSuccessListener { snapshot ->
                val other = snapshot.toUser()
                val requestReceived = (other.requestReceived ?: listOf())
                val friends = (other.friends ?: listOf())
                if (other.uid == User.INVALID_USER || requestReceived.contains(uid)
                    || friends.contains(uid)
                ) return@addOnSuccessListener

                val newRequest = requestReceived.toMutableList()
                newRequest.add(uid)
                other.requestReceived = newRequest
                getReferenceOfMine()?.apply{
                    val myDoc = this
                    get().addOnSuccessListener { snapshot ->
                        val me = snapshot.toUser()
                        val oUid = other.uid
                        val requestSent = me.requestSent ?: listOf()
                        val friends = me.friends ?: listOf()
                        if (oUid == null || me.uid == User.INVALID_USER
                            || requestSent.contains(oUid) || friends.contains(oUid)
                        )
                            return@addOnSuccessListener
                        val newSent = requestSent.toMutableList()
                        newSent.add(oUid)

                        me.requestSent = newSent
                        myDoc.set(me)
                        othDoc.set(other)
                    }
                }
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
    this.apply {
        val myDoc = this
        get().addOnSuccessListener { snapshot ->
            val me = snapshot.toUser()
            val myFriends = (me.friends ?: listOf())
            val myReceived = (me.requestReceived ?: listOf())
            if (me.uid == User.INVALID_USER || myFriends.contains(uid))
                return@addOnSuccessListener
            val newFriends = myFriends.toMutableList()
            newFriends.add(uid)
            val newReceived = myReceived.toMutableList().filter { it != uid }
            me.friends = newFriends
            me.requestReceived = newReceived
            getUserDocumentWith(uid)?.apply {
                val othDoc = this
                get().addOnSuccessListener { snapshot ->
                    val other = snapshot.toUser()
                    val otherSent = (other.requestSent ?: listOf())
                    val otherFriends = (other.friends ?: listOf())
                    if (other.uid == User.INVALID_USER)
                        return@addOnSuccessListener
                    val newOthSent = otherSent.filter { it != me.uid }
                    val newOthFriends = otherFriends.toMutableList()
                    newOthFriends.add(me.uid!!)
                    other.friends = newOthFriends
                    other.requestSent = newOthSent
                    othDoc.set(other)
                    myDoc.set(me)
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
