package com.example.firebasestoreandauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.firebasestoreandauth.databinding.ActivityLoginBinding
import com.example.firebasestoreandauth.databinding.CreateAccountDialogBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()
            doLogin(email, pass)
        }
    }

    private fun doLogin(email: String, pass: String) {
        Firebase.auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    startActivity(
                        Intent(this, StorageActivity::class.java)
                    )
                } else {
                    binding.textView.text = "don't have account? create account"
                    val itemBinding = CreateAccountDialogBinding.inflate(layoutInflater)
                    val accountDialog = AlertDialog.Builder(this)
                        .setTitle("create account")
                        .setView(itemBinding.root)
                        .setPositiveButton("create") { _, i ->
                            val newId = itemBinding.createId.text.toString()
                            val newPass = itemBinding.createPass.text.toString()
                            Firebase.auth.createUserWithEmailAndPassword(newId, newPass)
                                .addOnCompleteListener(this) {
                                    if (it.isSuccessful) {
                                        startActivity(
                                            Intent(this, StorageActivity::class.java)
                                        )
                                        finish()
                                    } else {
                                        Toast.makeText(this, "account create failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    binding.textView.setOnClickListener {
                        accountDialog.show()
                    }
                }
            }
    }
}