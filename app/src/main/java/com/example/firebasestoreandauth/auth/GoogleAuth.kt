package com.example.firebasestoreandauth.auth

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleAuth {
    companion object {
        private const val FIRESTORE = "FIRESTORE"
        private const val TAG = "GoogleSignIn"
        private const val RC_SIGN_IN = 9001

        fun firebaseAuthWithGoogle(
            idToken: String,
            onAuthCompleteListener: OnAuthCompleteListener
        ) {
            val auth = Firebase.auth
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "구글 OAuth를 사용하여 파이어베이스 로그인 : 성공")
                        if (task.result.additionalUserInfo?.isNewUser == true) {
                            Log.d(TAG, "새로 가입한 사용자입니다.")
                            onAuthCompleteListener.onNewUser()
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            onAuthCompleteListener.onSuccess()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        onAuthCompleteListener.onFailure(TAG, task)
                    }
                }
        }
    }
}

open interface OnAuthCompleteListener {
    /**
     * 로그인한 유저가 새로 가입한 유저인 경우
     */
    fun onNewUser() {

    }

    /**
     *  로그인한 유저의 정보를 이용해서 할 것들
     */
    fun onSuccess() {

    }

    /**
     * 로그인에 실패한 경우
     */
    fun onFailure(TAG: String, task: Task<AuthResult>) {
        Log.w(TAG, "signInWithCredential:failure", task.exception)
    }
}