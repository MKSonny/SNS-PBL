package com.example.firebasestoreandauth.fragments.auth.detail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.firebasestoreandauth.auth.LoginMainViewModel
import com.example.firebasestoreandauth.databinding.FragmentAuthSetBirthDayBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.extentions.toFirebase
import com.example.firebasestoreandauth.utils.filterPermission
import com.example.firebasestoreandauth.utils.providePermissions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class SetBirthdayFragment : Fragment() {
    private var _binding: FragmentAuthSetBirthDayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginMainViewModel by activityViewModels()
    private lateinit var requestPermLauncher: ActivityResultLauncher<Array<String>>
    var retryCount = 0

    // 갤러리에서 이미지 선택결과를 받고 파일 업로드
    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageURI = result.data?.data
            binding.selectedImage.setImageURI(imageURI)
            viewModel.setURI(imageURI)
        }
    }

    // 기본 사진앱에서 이미지 선택결과를 받고 파일 업로드
    private val photoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageURI = result.data?.data
            val bitmap = (result.data?.extras?.get("data")) as Bitmap
            binding.selectedImage.setImageBitmap(bitmap)
            viewModel.setURI(imageURI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAuthSetBirthDayBinding.inflate(inflater, container, false)
        requestPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
                val retry = map.filter { !it.value }.keys.toTypedArray()
                if (retry.isNotEmpty() && retryCount < 2) {
                    requestPermLauncher.launch(retry)
                    retryCount
                }
            }
        retryCount = 0
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val images = binding.selectedImage
        val submit = binding.submit

        binding.cardView.setOnClickListener {
            //selectGallery()
            AlertDialog.Builder(requireActivity()).apply {
                setTitle("사진촬영 및 갤러리 선택")
                setPositiveButton("Gallery") { _, _ -> selectGallery() }
                setNegativeButton("Photo") { _, _ -> selectPhoto() }
            }.show()

            binding.submit.setOnClickListener {
                val builder = UserProfileChangeRequest.Builder()
                builder.displayName = viewModel.nickname.value.toString()
                builder.photoUri = Uri.EMPTY
                Firebase.auth.currentUser?.updateProfile(builder.build())
                val baos = ByteArrayOutputStream()
                val bitmap = (binding.selectedImage.drawable as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val storageRef = Firebase.storage.reference
                val imageRef = storageRef.child("users/${Firebase.auth.currentUser?.uid}.jpg")
                imageRef.putBytes(data).addOnFailureListener {
                    Snackbar.make(requireView(), "계정데이터 업로드에 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                }.addOnSuccessListener {
                    User(
                        Firebase.auth.currentUser?.uid,
                        nickname = viewModel.nickname.value.toString(),
                        profileImage = "${it.metadata?.reference.toString()}",
                    ).toFirebase { requireActivity().finish() } //가입을 완료하고 프래그먼트를 종료

                }
            }
        }
    }

    // 기본 사진앱 호출
    private fun selectPhoto() {
        val perms = filterPermission(requireContext(), providePermissions())
        if (perms.isNotEmpty()) {
            requestPermLauncher.launch(perms)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoResult.launch(intent)
        }
    }

    //갤러리 호출
    private fun selectGallery() {
        val perms = filterPermission(requireContext(), providePermissions())
        if (perms.isNotEmpty()) {
            requestPermLauncher.launch(perms)
        } else {
            val intent = Intent(Intent.ACTION_PICK)

            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            imageResult.launch(intent)
        }
    }

}
