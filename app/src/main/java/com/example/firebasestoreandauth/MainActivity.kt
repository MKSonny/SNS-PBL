package com.example.firebasestoreandauth

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.firebasestoreandauth.test.AddPersonalInfoActivity
import com.example.firebasestoreandauth.wrapper.getImageReference
import com.example.firebasestoreandauth.wrapper.getUserDocumentReference
import com.example.firebasestoreandauth.auth.GoogleAuth
import com.example.firebasestoreandauth.auth.OnAuthCompleteListener
import com.example.firebasestoreandauth.databinding.ActivityMainBinding
import com.example.firebasestoreandauth.test.SearchFriendActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var gatherPersonalInfo: ActivityResultLauncher<Intent>
    private lateinit var searchForFriend: ActivityResultLauncher<Intent>
    private lateinit var storage: FirebaseStorage
    private lateinit var fbGoogleSignIn: ActivityResultLauncher<Intent>
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var defaultOnAuthCompleteListener: OnAuthCompleteListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val nhf = supportFragmentManager.findFragmentById(R.id.my_nav_host) as NavHostFragment
        val navController = nhf.navController
        binding.bottomNavigationView.setupWithNavController(navController)


        //Firebase 초기화
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        initAuth()

    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) auth.signOut()
        signIn()
    }

    private fun initAuth() {
        //로그인시 새로 등록한 유저이면 해당 액티비티를 시작
        gatherPersonalInfo =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                run {
                    if (result.resultCode == RESULT_OK) {
                        val nickname = result.data?.getStringExtra("Nickname")
                        val birthday = result.data?.getStringExtra("BirthDay")
                        println("${nickname}, ${birthday}")
                    }
                }
            }
        searchForFriend =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                run {
                    if (result.resultCode == RESULT_OK) {
                        val nickname = result.data?.getStringExtra("Nickname")
                        val birthday = result.data?.getStringExtra("BirthDay")
                        println("${nickname}, ${birthday}")
                    }
                }
            }
        //구글 OAuth를 이용한 로그인 설정
        fbGoogleSignIn =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                run {
                    if (it.resultCode == RESULT_OK) {
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
            this,
            with(GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)) {
                requestIdToken(getString(R.string.web_client_id))
                requestEmail()
                build()
            })
        //OAuth를 통과한 경우 사용하는 리스너
        defaultOnAuthCompleteListener = object : OnAuthCompleteListener {
            override fun onNewUser() {
                super.onNewUser()
                val intent = Intent(
                    this@MainActivity, AddPersonalInfoActivity::class.java
                )
                gatherPersonalInfo.launch(intent)
            }

            override fun onSuccess() {
                super.onSuccess()
                findUserRecord()
//                Snackbar.make(
//                    binding.root,
//                    "${auth.currentUser?.displayName.toString()}",
//                    Snackbar.LENGTH_SHORT
//                ).show()
            }

            override fun onFailure(TAG: String, task: Task<AuthResult>) {
                super.onFailure(TAG, task)
                Log.e(
                    TAG, "Google로그인은 성공했지만 Firebase인증은 실패했습니다.: " + task.exception?.message
                )
            }
        }
    }


    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInIntent.putExtra("requestCode", RC_SIGN_IN)
        googleSignInClient.revokeAccess()
        fbGoogleSignIn.launch(signInIntent)
    }


    private fun startPersonalInfoActivity() {
        val rIntent = Intent(this@MainActivity, AddPersonalInfoActivity::class.java)
        gatherPersonalInfo.launch(rIntent)
    }

    private fun getProfileImage(fileName: String) {
        val imageRef = getImageReference(fileName)
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
        }?.addOnFailureListener {
            println(it.message)
        }
    }

    private fun findUserRecord() {
        val reference = getUserDocumentReference(auth.currentUser?.uid.toString())
        reference?.get()?.addOnCompleteListener {
            if (it.isSuccessful) {
                val data = it.result.data
                if (data == null) {
                    Log.e(FIRESTORE, "유저 데이터가 존재하지 않습니다.")
                    startPersonalInfoActivity()
                    return@addOnCompleteListener
                }


                data["profileImage"]?.run {
                    val src = this as String
                    if (src.isNotEmpty()) getProfileImage(src)

                    //요청 중인 친구가 있는지 확인
                    val requestReceived = (data["requestReceived"]?.run {
                        this as List<*>
                    }) ?: emptyList<String>()

                    if (requestReceived.isNotEmpty()) {
                        Log.d("Friends", "요청 중인 친구가 있습니다.")
//                        Snackbar.make(
//                            binding.root,
//                            "요청 중인 친구가 있습니다. 총 ${requestReceived.size}명",
//                            Snackbar.LENGTH_SHORT
//                        ).show()
                    } //요청 중인 친구가 있다면 사용자에게 알림
                }
                reference.addSnapshotListener { snapshot, _ ->
                    run {
                        if (snapshot != null && snapshot.data != null) {
                            for (item in snapshot.data!!)
                                println("${item.key} ${item.value}")
                        }
                    }
                }
            } else {
                Log.e(FIRESTORE, it.exception.toString())
            }
        }
    }

    companion object {
        private const val FIRESTORE = "FIRESTORE"
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

}