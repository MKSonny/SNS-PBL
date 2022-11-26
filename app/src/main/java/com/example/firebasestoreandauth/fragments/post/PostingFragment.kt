package com.example.firebasestoreandauth.fragments.post

import android.annotation.SuppressLint
import android.content.ContentUris
import android.graphics.Bitmap
import android.net.Uri
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
import java.io.ByteArrayOutputStream
import java.util.*

class PostingFragment : Fragment(R.layout.fragment_profile_posting) {
    private var _binding: FragmentProfilePostingBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: ProfileViewModel
    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore

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

    @SuppressLint("WrongThread")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentProfilePostingBinding.bind(view)

        storage = Firebase.storage

        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        val imgUrl = viewModel.getPos()
        val bitmap = viewModel.getbit()
        binding.postImage.setImageURI(imgUrl)

        val now = System.currentTimeMillis()
        val date = Date(now)

        binding.posting.setOnClickListener {

            val comment = binding.comments.text.toString()
            val like = 0
            val whoPosted = "${Firebase.auth.currentUser?.uid}"

            val tampComments = java.util.ArrayList<Map<String, String>>()
            tampComments.add(
                mapOf("${Firebase.auth.currentUser?.uid}" to comment)
            )

            val forPostId = db.collection("PostInfo").document()
            val itemMap = hashMapOf(
                "likes" to like,
                "whoPosted" to whoPosted,
                "time" to FieldValue.serverTimestamp(),
                "testing" to tampComments,
                "img" to "gs://sns-pbl.appspot.com/${date}",
                "post_id" to forPostId.id
            )

            val imageFile = viewModel.getFile()


            forPostId.set(itemMap).addOnSuccessListener {
                if (viewModel.getcat() == 1 && bitmap != null){
                    val cameraRef = storage.reference.child("${date}")
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    val uploadTask = cameraRef.putBytes(data)
                    uploadTask.addOnFailureListener {
                    }.addOnSuccessListener { taskSnapshot ->
                    }
                    viewModel.setcat(0)
                }
                else{
                    uploadFile(imageFile, date.toString())
                }
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