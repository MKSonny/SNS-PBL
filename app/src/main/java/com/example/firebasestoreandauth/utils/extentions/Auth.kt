@file:Suppress("UNCHECKED_CAST")

package com.example.firebasestoreandauth.utils.extentions

import android.util.Log
import com.google.firebase.auth.FirebaseAuth


fun FirebaseAuth.signOut() {
    Log.d("FirebaseAuth", "${this.currentUser?.displayName}이/가 로그아웃 했습니다.")
    this.signOut()
}

