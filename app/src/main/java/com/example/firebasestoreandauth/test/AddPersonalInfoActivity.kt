package com.example.firebasestoreandauth.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.ActivityAddPersonalInfoBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddPersonalInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddPersonalInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.uploadPersonalInfoButton.setOnClickListener {
            val nickname = binding.editNickName.text.toString()
            val dayOfBirth = binding.editBirthDate.text.toString()
            val rIntent = Intent()
            rIntent.putExtra("Nickname", nickname)
            rIntent.putExtra("BirthDay", dayOfBirth)
            setResult(RESULT_OK, rIntent)
            uploadToFirebase(nickname, dayOfBirth)
            finish()
        }
    }

    /**
     *
     */
    private fun uploadToFirebase(nickName: String, date: String) {
        val db = Firebase.firestore
        val uid = Firebase.auth.currentUser!!.uid
        db.collection("Users").document(uid).set(User(UID=uid, NickName = nickName, BirthDay = date, profileImage = ""))
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    println(it.result)
                }
                else{
                    println(it.exception)
                }
            }

    }
}