package com.example.firebasestoreandauth.auth.ui.email

import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.auth.LoginMainViewModel
import com.example.firebasestoreandauth.databinding.FragmentEmailSignupBinding
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpFragment : Fragment() {
    private val loginViewModel: LoginMainViewModel by viewModels()
    private var _binding: FragmentEmailSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmailSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val signUpButton = binding.signup
        val loadingProgressBar = binding.loading

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                signUpButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            }
        )
        signUpButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    val myDoc = getReferenceOfMine()?.get()?.isSuccessful
                    if (myDoc == false)
                        view?.findNavController()
                            ?.navigate(R.id.action_emailSignUpFragment_to_setNickNameFragment)
//                    activity?.finish()
                } else {
                    val activity = activity
                    if (activity != null)
                        Snackbar.make(
                            activity.findViewById(android.R.id.content),
                            "가입에 실패했습니다.${it.exception.toString()}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    loadingProgressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

}