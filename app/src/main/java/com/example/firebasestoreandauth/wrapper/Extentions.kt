@file:Suppress("UNCHECKED_CAST")

package com.example.firebasestoreandauth.wrapper

import android.util.Log
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.Item
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

/**
 * 사용자를 찾는 DocumentSnapshot 에서
 * User를 추출하는 확장메서드
 */
fun DocumentSnapshot.toUser(): User {
    return this.data.let {
        User(
            UID = it?.get("uid").toString() ?: User.INVALID_USER,
            NickName = it?.get("nickName").toString() ?: User.INVALID_USER,
            BirthDay = it?.get("birthDay").toString() ?: User.INVALID_USER,
            profileImage = it?.get("profileImage").toString() ?: User.INVALID_USER,
            friends = it?.get("friends")
                ?.run { this as List<*> } as List<String>? ?: listOf(),
            requestSent = it?.get("requestSent")
                ?.run { this as List<*> } as List<String>? ?: listOf(),
            requestReceived = it?.get("requestReceived")
                ?.run { this as List<*> } as List<String>? ?: listOf(),
        )
    }

}

/**
 *  친구요청을 삭제하는 Extension Function
 *  @param uid: 친구추가 요청하는 사용자의 UID
 */
fun DocumentReference.sendRequestToFriend(uid: String) {
    val reference = this
    this.get().addOnCompleteListener { response ->
        run {
            val data = response.result.data
            val requestReceived = data?.get("requestReceived")?.run {
                this as MutableList<String>?
            }
            if (requestReceived != null) {
                for (item in requestReceived) {
                    if (item == uid) {
                        return@addOnCompleteListener
                    }
                }
                requestReceived.add(uid)
                this.update(mapOf("requestReceived" to requestReceived))
            }
        }
    }
}

/**
 *  친구요청을 삭제하는 Extension Function
 *  @param uid 삭제하려는 대상
 */
fun DocumentReference.removeReceivedRequest(uid: String) {
    val reference = this
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

fun DocumentSnapshot.toPostItem(): Item {
    val whoPosted = (this["whoPosted"] ?: Item.INVALID_ITEM) as String
    val document = this
    val uid = this.id
    val profile_img = (document["profile_img"] ?: "") as String
    val imgUrl =
        (document["img"] ?: "") as String
    val likes =
        (document["likes"] ?: "") as Number
    val time =
        (document["time"] ?: Timestamp.now()) as Timestamp
    val comments =
        (document["testing"] ?: ArrayList<Map<String, String>>()) as ArrayList<Map<String, String>>

    return Item(
        profile_img, uid, imgUrl,
        likes, time, whoPosted, comments
    )

}

fun FirebaseAuth.signOut() {
    Log.d("FirebaseAuth", "${this.currentUser?.displayName}이/가 로그아웃 했습니다.")
    this.signOut()
}
