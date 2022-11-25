@file:Suppress("UNCHECKED_CAST")

package com.example.firebasestoreandauth.utils.extentions

import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.firebasestoreandauth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


fun FirebaseAuth.signOut() {
    Log.d("FirebaseAuth", "${this.currentUser?.displayName}이/가 로그아웃 했습니다.")
    this.signOut()
}

fun Fragment.signOut() {
    val auth = Firebase.auth
    if (auth.currentUser != null)
        auth.signOut()
    startActivity(Intent(this.requireContext(), LoginActivity::class.java))
}

