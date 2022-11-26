package com.example.firebasestoreandauth.fragments.post

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.FragmentProfilePostingBinding
import com.example.firebasestoreandauth.utils.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class PostingFragment : Fragment(R.layout.fragment_profile_posting) {
    private var _binding: FragmentProfilePostingBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: ProfileViewModel
    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore
    val docPostRef = db.collection("PostInfo")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfilePostingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentProfilePostingBinding.bind(view)

        storage = Firebase.storage

        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        val imgUrl = viewModel.getPos()
        if (imgUrl != null)
            binding.postImage.setImageURI(imgUrl)
        else {
            if (viewModel.bitmap != null)
                binding.postImage.setImageBitmap(viewModel.bitmap)
            viewModel.bitmap = null
        }

        binding.posting.setOnClickListener {

            val comment = binding.comments.text.toString()
            val like = 0
            val whoPosted = "${Firebase.auth.currentUser?.uid}"

            val tampComments = java.util.ArrayList<Map<String, String>>()
            tampComments.add(
                mapOf("${Firebase.auth.currentUser?.uid}" to comment)
            )

            val itemMap = hashMapOf(
                "likes" to like,
                "whoPosted" to whoPosted,
                "time" to FieldValue.serverTimestamp(),
                "testing" to tampComments,
            )

            val imageFile = viewModel.getFile()
            val imageName = viewModel.getName()

            docPostRef.add(itemMap)
                .addOnSuccessListener {
                    uploadFile(imageFile, imageName)
                    val col = it.id
                    val idMap = hashMapOf(
                        "img" to "gs://sns-pbl.appspot.com/${viewModel.getName()}",
                        "post_id" to col
                    )
                    docPostRef.document(it.id).update(idMap as Map<String, Any>)
                    findNavController().navigate(R.id.action_postingFragment_to_profileFragment)

                }.addOnFailureListener {
                    Snackbar.make(binding.root, "Upload fail.", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadFile(file_id: Long?, fileName: String?) {
        file_id ?: return
        val imageRef = storage.reference.child("${fileName}")
        val contentUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, file_id)
        imageRef.putFile(contentUri).addOnCompleteListener {
            if (it.isSuccessful) {
            }
        }
    }

}