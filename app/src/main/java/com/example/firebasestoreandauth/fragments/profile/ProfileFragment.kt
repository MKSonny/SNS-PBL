package com.example.firebasestoreandauth.fragments.profile

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.FragmentProfileMainBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.ProfileViewModel
import com.example.firebasestoreandauth.utils.extentions.signOut
import com.example.firebasestoreandauth.utils.extentions.toUser
import com.example.firebasestoreandauth.utils.filterPermission
import com.example.firebasestoreandauth.utils.getReferenceOfMine
import com.example.firebasestoreandauth.utils.providePermissions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

// 게시물 보여주기
class ProfileFragment : Fragment(R.layout.fragment_profile_main) {
    private var _binding: FragmentProfileMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var requestPermLauncher: ActivityResultLauncher<Array<String>>
    lateinit var storage: FirebaseStorage
    var retryCount = 0
    private val db: FirebaseFirestore = Firebase.firestore
    val docUserRef = db.collection("Users").document("${Firebase.auth.currentUser?.uid}")
    val colPostRef = db.collection("post")

    lateinit var viewModel: ProfileViewModel
    lateinit var filePath: String

    companion object {
        const val REQ_GALLERY = 1
        const val REQ_PERMISSION_CAMERA = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileMainBinding.inflate(inflater, container, false)
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

    // 갤러리에서 이미지 선택결과를 받고 뷰모델에 저장
    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageURI = result.data?.data

            imageURI?.let {
                val imageFile = getRealPathFromURI(it)
                val imageName = getRealPathFromNAME(it)
                viewModel.setFile(imageFile)
                viewModel.setName(imageName)
            }
            viewModel.setPos(imageURI)

        }

        findNavController().navigate(R.id.action_profileFragment_to_postingFragment)

    }

    // 기본 사진앱에서 이미지 선택결과를 받고 뷰모델에 저장
    private val photoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {

            /*val option = BitmapFactory.Options()
            option.inSampleSize = 10
            val bitmap = BitmapFactory.decodeFile(result.toString(), option)
            viewModel.setbit(bitmap)*/
            val imageURI = result.data?.data
            imageURI?.let {

                val imageFile = getRealPathFromURI(it)
                val imageName = getRealPathFromNAME(it)
                viewModel.setFile(imageFile)
                viewModel.setName(imageName)

            }

            viewModel.setPos(imageURI)

        }

        findNavController().navigate(R.id.action_profileFragment_to_postingFragment)
    }

    // 개시물 id 가져오기
    fun getRealPathFromURI(uri: Uri): Long {
        var columnIndex = 0
        val proj = arrayOf(MediaStore.Images.ImageColumns._ID)
        val cursor = requireActivity().contentResolver.query(uri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
        }
        val result = cursor.getLong(columnIndex)
        cursor.close()
        return result
    }

    // 개시물 name 가져오기
    fun getRealPathFromNAME(uri: Uri): String {
        var columnIndex = 0
        val proj = arrayOf(MediaStore.Images.ImageColumns.DISPLAY_NAME)
        val cursor = requireActivity().contentResolver.query(uri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
        }
        val result = cursor.getString(columnIndex)
        cursor.close()
        return result
    }

    // 갤러리에서 이미지 선택결과를 받고 프로필화면으로 전환
    private val profileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageURI = result.data?.data
            imageURI?.let {
                binding.profile.setImageURI(imageURI)
                val imageFile = getRealPathFromURI(it)
                val imageName = getRealPathFromNAME(it)
                uploadFile(imageFile, imageName)

                val idMap = hashMapOf(
                    "profileImage" to "gs://sns-pbl.appspot.com/${imageName}"
                )
                getReferenceOfMine()?.update(idMap as Map<String, Any>)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentProfileMainBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        storage = Firebase.storage


        // 유저 프로필 이미지에서 url 가져와서 띄우기
        getReferenceOfMine()?.apply {
            get().addOnSuccessListener { // it: DocumentSnapshot
                viewModel.setPro(it["profileImage"].toString())
                val ref = viewModel.getPro().toString()
                if (ref.startsWith("gs:")) {
//                    val imageRef2 = storage.getReferenceFromUrl(ref)
                    val url = storage.getReferenceFromUrl(ref)
                    displayImageRef(url, binding.profile)
                }
            }.addOnFailureListener {
            }


            //게시물 6개 출력
            colPostRef.whereEqualTo("whoPosted", "${Firebase.auth.currentUser?.uid}").get()
                .addOnSuccessListener { documents ->
                    var size = 1
                    for (doc in documents) {
                        viewModel.setPro(doc["imgUrl"].toString())
                        val postRef1 = storage.getReferenceFromUrl(viewModel.getPro().toString())
                        if (size == 1)
                            displayImageRef(postRef1, binding.imageView)
                        else if (size == 2)
                            displayImageRef(postRef1, binding.imageView2)
                        else if (size == 3)
                            displayImageRef(postRef1, binding.imageView3)
                        else if (size == 4)
                            displayImageRef(postRef1, binding.imageView4)
                        else if (size == 5)
                            displayImageRef(postRef1, binding.imageView5)
                        else if (size == 6) {
                            displayImageRef(postRef1, binding.imageView6)
                            break
                        }
                        size++
                    }
                }.addOnFailureListener {
                }
            // 개시물수, 친구수 출력
            queryItem()
            binding.settingButton.setOnClickListener {
                AlertDialog.Builder(requireActivity()).apply {
                    setTitle("로그아웃하시겠습니까?")
                    setPositiveButton("예") { _, _ -> signOut() }
                    setNegativeButton("아니오") { _, _ -> }

                }.show()

            }

            // 업로드 버튼
            binding.buttonUpload.setOnClickListener {
                //selectGallery()
                AlertDialog.Builder(requireActivity()).apply {
                    setTitle("사진촬영 및 갤러리 선택")
                    setPositiveButton("Gallery") { _, _ -> selectGallery() }
                    setNegativeButton("Photo") { _, _ -> selectPhoto() }

                }.show()
            }
            // 프로필 변경 버튼
            binding.buttonProfile.setOnClickListener {
                selectGalleryProfile()
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

    // 게시물수, 친구수 출력
    private fun queryItem() { // 1번문제
        getReferenceOfMine()?.apply {
            get().addOnSuccessListener { // it: DocumentSnapshot
                val user = it.toUser()
                if (user.uid == null || user.uid == User.INVALID_USER || user.uid!!.isEmpty())
                    return@addOnSuccessListener
                val friend = (user.friends ?: listOf())
                binding.friendNumber.text = friend.size.toString()

                colPostRef.whereEqualTo("whoPosted", "${user.uid}").get()
                    .addOnSuccessListener { documents ->
                        var size = 0
                        for (doc in documents) {
                            size++
                        }
                        binding.postNumber.setText(size.toString())
                    }.addOnFailureListener { }
            }.addOnFailureListener { }
        }
    }


    // 기본 사진앱 호출
    private fun selectPhoto() {
        val perms = filterPermission(requireActivity(), providePermissions())
        if (perms.isNotEmpty()) {
            requestPermLauncher.launch(perms)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoResult.launch(intent)
        }
    }

    //갤러리 호출
    private fun selectGallery() {
        val perms = filterPermission(requireActivity(), providePermissions())

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

    //갤러리 호출 후 프로필사진 변경
    private fun selectGalleryProfile() {
        val writePermission = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQ_GALLERY
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK)

            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )

            profileResult.launch(intent)
        }
    }

    // 스토리지에서 이미지 가져와서 표시
    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener {
        }
    }


}