package com.example.firebasestoreandauth.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.pblsns.firebase.wrapper.addPendingFriends
import com.android.pblsns.firebase.wrapper.getUserDocumentReference
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.ActivitySearchFriendBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchFriendBinding
    private var queryResult = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.searchButton.setOnClickListener {
            val keyword = binding.keyword.text.toString() //keyword : Email, NickName, RealName
            val db = Firebase.firestore
            db.collection("Users").whereEqualTo("nickName", keyword)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val documentRef = it.result.documents
                        queryResult.clear()
                        documentRef.map {
                            queryResult.add(
                                //TODO 컨버터 작성
                                User(
                                    UID = it.get("uid").toString(),
                                    NickName = it.get("nickName").toString(),
                                    pendingFriends = it.get("pendingFriends")
                                        ?.run { this as List<*> } as List<String>?,
                                    BirthDay = it.get("birthDay").toString(),
                                    friends = it.get("friends")
                                        .run { this as List<*> } as List<String>?,
                                    profileImage = it.get("profileImage").toString()
                                )
                            )
                            var string = ""
                            for (item in queryResult) {
                                string += item.UID + "\n"
                            }
                            binding.textView6.text = string
                        }
                    } else {
                        println(it.exception)
                    }
                }

        }
        binding.requestToFriend.setOnClickListener {
            val uid = Firebase.auth.currentUser?.uid
            val idx = binding.editIndexButton
                .text
                .toString().toInt()
            if (Firebase.auth.currentUser != null) {
                //For test purpose only!
                if (idx > queryResult.size - 1)
                    return@setOnClickListener
                val uid = queryResult[idx].UID
                if (uid != null) {
                    val reference = getUserDocumentReference(uid)
                    reference?.get()?.addOnSuccessListener { _ ->
                        reference.addPendingFriends(uid)
                    }
                }
            }
        }
    }
}