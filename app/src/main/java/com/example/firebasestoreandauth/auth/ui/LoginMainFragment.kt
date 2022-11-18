package com.example.firebasestoreandauth.auth.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.auth.GoogleAuth
import com.example.firebasestoreandauth.auth.LoginMainViewModel
import com.example.firebasestoreandauth.auth.OnAuthCompleteListener
import com.example.firebasestoreandauth.databinding.FragmentLoginMainBinding
import com.example.firebasestoreandauth.test.AddPersonalInfoActivity
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

class LoginMainFragment : Fragment() {
    private lateinit var binding: FragmentLoginMainBinding
    private lateinit var fbGoogleSignIn: ActivityResultLauncher<Intent?>
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        fun newInstance() = LoginMainFragment()
        private const val FIRESTORE = "FIRESTORE"
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    private val viewModel: LoginMainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authInit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginMainBinding.inflate(inflater)
        binding.loginStartWithEmailButton.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_loginMainFragment_to_emailLoginFragment)
        }
        binding.startWithGoogleButton.setOnClickListener {
            signInWithGoogle()
        }
        return binding.root
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInIntent.putExtra("requestCode", RC_SIGN_IN)
        googleSignInClient.revokeAccess()
        fbGoogleSignIn.launch(signInIntent)
    }

    private val defaultOnAuthCompleteListener = object : OnAuthCompleteListener {
        override fun onNewUser() {
            super.onNewUser()
        }

        override fun onSuccess() {
            super.onSuccess()
//            if (getReferenceOfMine() == null) {
//                view?.findNavController()
//                    ?.navigate(R.id.action_loginMainFragment_to_setNickNameFragment2)
//            }
//            activity?.finish()
        }

        override fun onFailure(TAG: String, task: Task<AuthResult>) {
            super.onFailure(TAG, task)
            Log.e(
                TAG, "Google로그인은 성공했지만 Firebase인증은 실패했습니다.: " + task.exception?.message
            )
        }
    }

    private fun authInit() {
        //구글 OAuth를 이용한 로그인 설정
        fbGoogleSignIn =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                run {
                    if (it.resultCode == AppCompatActivity.RESULT_OK) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            val account = task.getResult(ApiException::class.java)!!
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                            GoogleAuth.firebaseAuthWithGoogle(
                                account.idToken.toString(), defaultOnAuthCompleteListener
                            )
                        } catch (e: ApiException) {
                            // Google Sign In failed, update UI appropriately
                            Log.w(TAG, "Google sign in failed", e)
                        }
                    }
                }
            }
        googleSignInClient = GoogleSignIn.getClient(
            requireContext(),
            with(GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)) {
                requestIdToken(getString(R.string.web_client_id))
                requestEmail()
                build()
            })
    }
}

