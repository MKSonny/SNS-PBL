package com.example.firebasestoreandauth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.firebasestoreandauth.auth.LoginActivity
import com.example.firebasestoreandauth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    lateinit var binding: ActivityMainBinding
    private lateinit var appbarc: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Navigation 초기화
        val nhf = supportFragmentManager.findFragmentById(R.id.my_nav_host) as NavHostFragment
        val navController = nhf.navController
        binding.bottomNavigationView.setupWithNavController(navController)
        appbarc = AppBarConfiguration(setOf(R.id.profileFragment, R.id.friendsFragment, R.id.postFragment))
        //setupActionBarWithNavController(nhf.navController, appBarc)

        //Firebase 초기화
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null)
            startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.my_nav_host)
        return navController.navigateUp(appbarc)
                || super.onSupportNavigateUp()
    }

    fun hideBottomNav(state: Boolean) {
        if (state) binding.bottomNavigationView.visibility =
            View.GONE else binding.bottomNavigationView.visibility = View.VISIBLE
    }

}