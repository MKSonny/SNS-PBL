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
import androidx.navigation.findNavController
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.auth.LoginMainViewModel
import com.example.firebasestoreandauth.databinding.FragmentSetNickNameBinding
import com.example.firebasestoreandauth.wrapper.isThisNicknameInUse
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetNickNameFragment : Fragment() {
    private var _binding: FragmentSetNickNameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginMainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSetNickNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nickname = binding.editNickName
        val goodNickname = binding.usableNickname
        val nextButton = binding.button2
        val store = Firebase.firestore
        goodNickname.visibility = View.INVISIBLE
        nextButton.isEnabled = false

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val firestore = Firebase.firestore
                if (nickname.text.isEmpty()) return
                val userCollection = firestore.collection("Users")
                val query = userCollection.whereEqualTo("nickname", nickname.text.toString())
                query.get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val documents = it.result.documents
                        if (documents.isEmpty()) {
                            nickname.contentDescription = "Good"
                            goodNickname.visibility = View.VISIBLE
                            nextButton.isEnabled = true
                        } else if (documents.isNotEmpty()) {
                            nickname.error = "이미 사용중인 닉네임입니다."
                            goodNickname.visibility = View.INVISIBLE
                            nextButton.isEnabled = false
                        }
                    }
                }
            }
        }
        binding.editNickName.addTextChangedListener(afterTextChangedListener)
        binding.button2.setOnClickListener {
            viewModel.nickname.value = (nickname.text.toString())
            view?.findNavController()
                ?.navigate(R.id.action_setNickNameFragment_to_setBirthdayFragment)
        }
    }


}