@file:Suppress("UNCHECKED_CAST")

package com.android.pblsns.firebase.wrapper

import android.util.Log
import com.example.firebasestoreandauth.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference

/**
 *  친구요청을 삭제하는 Extension Function
 *  @param uid: 친구추가 요청하는 사용자의 UID
 */
fun DocumentReference.addPendingFriends(uid: String) {
    val reference = this
    this.get().addOnCompleteListener { response ->
        run {
            val data = response.result.data
            val pendingFriends = data?.get("pendingFriends")?.run {
                this as MutableList<String>?
            }
            if (pendingFriends != null) {
                for (item in pendingFriends) {
                    if (item == uid) {
                        return@addOnCompleteListener
                    }
                }
                pendingFriends.add(uid)
                this.update(mapOf("pendingFriends" to pendingFriends))
            }
        }
    }
}

/**
 *  친구요청을 삭제하는 Extension Function
 *  @param uid 삭제하려는 대상
 */
fun DocumentReference.removePendingFriends(uid: String) {
    val reference = this
    this.get().addOnCompleteListener { response ->
        run {
            val data = response.result.data
            val pendingFriends = data?.get("pendingFriends")?.run {
                this as MutableList<String>?
            }
            if (pendingFriends != null) {
                for (item in pendingFriends) {
                    if (item == uid) {
                        pendingFriends.remove(uid)
                    }
                }
                this.update(mapOf("pendingFriends" to pendingFriends))
            }
        }
    }
}
fun FirebaseAuth.signOut() {
    Log.d("FirebaseAuth", "${this.currentUser?.displayName}이/가 로그아웃 했습니다.")
    this.signOut()
}
