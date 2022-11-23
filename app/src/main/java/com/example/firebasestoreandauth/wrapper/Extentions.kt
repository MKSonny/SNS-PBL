@file:Suppress("UNCHECKED_CAST")

package com.example.firebasestoreandauth.wrapper

import android.util.Log
import com.example.firebasestoreandauth.DTO.Item
import com.example.firebasestoreandauth.DTO.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Types.TIMESTAMP

/**
 * 사용자를 찾는 DocumentSnapshot 에서
 * User를 추출하는 확장메서드
 */

fun DocumentSnapshot.toItem(): Item {
    return this.data.let {
        Item(
            profile_img = (it?.get("profile_img") ?: "gs://sns-pbl.appspot.com/post_img_err.png") as String,
            postImgUrl = (it?.get("img") ?: "gs://sns-pbl.appspot.com/post_img_err.png") as String,
            likes = (it?.get("likes") ?: 0) as Number,
            //profileImage = it?.get("profileImage").toString() ?: User.INVALID_USER,
            whoPosted = (it?.get("whoPosted") ?: User.INVALID_USER) as String,
            time = ((it?.get("time")) ?: Timestamp(0, 0) ) as Timestamp,
//            comments = (it?.get("testing")
//                ?.run { this as ArrayList<Map<String, String>> } ?: listOf()) as ArrayList<Map<String, String>>,
            comments = (it?.get("testing")
                ?.run { this as ArrayList<Map<String, String>> } ?: listOf(mapOf("댓글" to "오류"))) as ArrayList<Map<String, String>>,
            postId = ((it?.get("post_id") ?: User.INVALID_USER) as String)
        )
    }
}


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
                if (other.uid == User.INVALID_USER||other.uid == uid || requestReceived.contains(uid)
                    || friends.contains(uid)
                ) return@addOnSuccessListener

                val newRequest = requestReceived.toMutableList()
                newRequest.add(uid)
                other.requestReceived = newRequest
                getReferenceOfMine()?.apply {
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
 *  친구찾기를 거절하는 기능
 *  @param uid 삭제하려는 대상
 */
fun DocumentReference.rejectFriendRequest() {
    this.apply {
        val usr = Firebase.auth.currentUser ?: return
        val uid = usr.uid
        val othDoc = this
        get().addOnSuccessListener { snapshot ->
            val other = snapshot.toUser()
            val otherSent = (other.requestSent ?: listOf())
            if (other.uid == User.INVALID_USER)
                return@addOnSuccessListener
            otherSent.filter { it != uid }
            other.requestSent = otherSent
            getReferenceOfMine()?.apply {
                val myDoc = this
                get().addOnSuccessListener { mySnapshot ->
                    val me = mySnapshot.toUser()
                    val received = (me.requestReceived ?: listOf())
                    if (me.uid == User.INVALID_USER)
                        return@addOnSuccessListener
                    val newReceived = received.filter { it != other.uid }
                    myDoc.set(me)
                    othDoc.set(other)
                }
            }
        }
    }
}

/**
 * 친구 찾기를 수락하는 기능
 */
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

/**
 * 친구를 삭제하는 기능
 */
fun DocumentReference.deleteFriend() {
    this.apply {
        val othDoc = this
        get().addOnSuccessListener { snapshot ->
            val other = snapshot.toUser()
            val othFriends = (other.friends ?: listOf())
            val auth = Firebase.auth
            if (other.uid == User.INVALID_USER || auth.currentUser == null)
                return@addOnSuccessListener
            val newOthFriends = othFriends.filter { it != auth.currentUser!!.uid }
            other.friends = newOthFriends
            getReferenceOfMine()?.apply {
                val myDoc = this
                get().addOnSuccessListener { mySnapshot ->
                    val me = mySnapshot.toUser()
                    val myFriends = (me.friends?: listOf())
                    if (me.uid == User.INVALID_USER)
                        return@addOnSuccessListener
                    val newMyFriends = myFriends.filter { it != other.uid }
                    me.friends =newMyFriends
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
