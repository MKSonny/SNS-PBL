package com.example.firebasestoreandauth.utils.extentions

import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.getReferenceOfMine
import com.example.firebasestoreandauth.utils.getUserDocumentWith
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.ktx.Firebase

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
                if (other.uid == User.INVALID_USER || other.uid == uid || requestReceived.contains(
                        uid
                    )
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

            other.requestSent = otherSent.filter { it != uid }
            getReferenceOfMine()?.apply {
                val myDoc = this
                get().addOnSuccessListener { mySnapshot ->
                    val me = mySnapshot.toUser()
                    val received = (me.requestReceived ?: listOf())
                    if (me.uid == User.INVALID_USER)
                        return@addOnSuccessListener
                    me.requestReceived = received.filter { it != other.uid }
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
                    val myFriends = (me.friends ?: listOf())
                    if (me.uid == User.INVALID_USER)
                        return@addOnSuccessListener
                    val newMyFriends = myFriends.filter { it != other.uid }
                    me.friends = newMyFriends
                    othDoc.set(other)
                    myDoc.set(me)
                }
            }

        }
    }
}