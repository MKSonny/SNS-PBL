package com.example.firebasestoreandauth.auth.ui.detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.auth.LoginMainViewModel
import com.example.firebasestoreandauth.databinding.FragmentSetBirthDayBinding
import com.example.firebasestoreandauth.wrapper.toFirebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SetBirthdayFragment : Fragment() {
    private var _binding: FragmentSetBirthDayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginMainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSetBirthDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val birthday = binding.editNickName
        val nextButton = binding.button2
        val store = Firebase.firestore
        nextButton.isEnabled = false

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                nextButton.isEnabled = true
            }
        }
        binding.editNickName.addTextChangedListener(afterTextChangedListener)
        binding.button2.setOnClickListener {
            viewModel.setBirthday(birthday.text.toString())
            User(
                Firebase.auth.currentUser?.uid,
                nickname = viewModel.nickname.value.toString(),
                profileImage = null,
            ).toFirebase()
        }
    }


}