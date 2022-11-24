package com.example.firebasestoreandauth.utils.extentions

import com.example.firebasestoreandauth.dto.Item
import com.example.firebasestoreandauth.dto.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

/**
 * 사용자를 찾는 DocumentSnapshot 에서
 * User를 추출하는 확장메서드
 */

fun DocumentSnapshot.toItem(): Item {
    return this.data.let {
        Item(
            profile_img = (it?.get("profile_img")
                ?: "gs://sns-pbl.appspot.com/post_img_err.png") as String,
            postImgUrl = (it?.get("img") ?: "gs://sns-pbl.appspot.com/post_img_err.png") as String,
            likes = (it?.get("likes") ?: 0) as Number,
            //profileImage = it?.get("profileImage").toString() ?: User.INVALID_USER,
            whoPosted = (it?.get("whoPosted") ?: User.INVALID_USER) as String,
            time = ((it?.get("time")) ?: Timestamp(0, 0)) as Timestamp,
//            comments = (it?.get("testing")
//                ?.run { this as ArrayList<Map<String, String>> } ?: listOf()) as ArrayList<Map<String, String>>,
            comments = (it?.get("testing")
                ?.run { this as ArrayList<Map<String, String>> }
                ?: listOf(mapOf("댓글" to "오류"))) as ArrayList<Map<String, String>>,
            postId = ((it?.get("post_id") ?: User.INVALID_USER) as String)
        )
    }
}