package com.example.firebasestoreandauth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.ProfileLayoutBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment(R.layout.profile_layout) {

    private var _binding: ProfileLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = ProfileLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signOutButton.setOnClickListener {
            if (Firebase.auth.currentUser != null)
                Firebase.auth.signOut()

        }
        binding.changename.setOnClickListener {
            val user = Firebase.auth.currentUser
            if (user != null) {
                val builder = UserProfileChangeRequest.Builder()
                builder.displayName = "name"
                Firebase.auth.currentUser?.updateProfile(builder.build())
            }

        }

        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            Firebase.auth.currentUser?.displayName ?: "로그인 상태 아님",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}