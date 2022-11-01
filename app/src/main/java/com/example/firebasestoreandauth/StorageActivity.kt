package com.example.firebasestoreandauth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.firebasestoreandauth.databinding.ActivityStorageBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class StorageActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage
    lateinit var binding: ActivityStorageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.auth.currentUser ?: finish()

        storage = Firebase.storage
        val storageRef = storage.reference

    }
    //private fun setFragment() {
    //    val transaction = supportFragmentManager.beginTransaction()
    //        .add()
    //}
}