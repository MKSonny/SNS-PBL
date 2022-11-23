package com.example.firebasestoreandauth


import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.*
import com.example.firebasestoreandauth.wrapper.ProfileViewModel
import com.example.firebasestoreandauth.wrapper.toItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*

class PostFragment : Fragment(R.layout.post_layout) {
    val db: FirebaseFirestore = Firebase.firestore
    private var snapshotListener: ListenerRegistration? = null
    lateinit var adapter: MyAdapter
    var cnt = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        val navigate = findNavController()

        adapter = MyAdapter(db, navigate, viewModel)

        var friends = ArrayList<String>()
        // 로그인 후 나의 문서 코드를 document 안에 수정합니다.


        //}
        //for (it in friends)
        println("adfadfadfadfadf444#$$" + friends.size)

        var nowRefresh = false

        //db.collection("SonUsers").document("UXEKfhpQLYnVFXCTFl9P")
        //getReferenceOfMine()?.get()?.addOnSuccessListener {
        db.collection("SonUsers").document("UXEKfhpQLYnVFXCTFl9P").get().addOnSuccessListener {
            val friends = it["friends"] as ArrayList<String>
            friends.add("UXEKfhpQLYnVFXCTFl9P") // 자기 게시물도 볼 수 있도록
            db.collection("PostInfo").orderBy("time", Query.Direction.DESCENDING).get()
                .addOnSuccessListener {
                    for (doc in it) {
                        val post = doc.toItem()
                        for (friend in friends) {
                            if (post.whoPosted == friend) {
                                viewModel.addItem(post)
                            }
                        }
                        nowRefresh = true
                    }

                    nowRefresh = true
                }
            snapshotListener = db.collection("PostInfo").addSnapshotListener { snapshot, error ->
                if (nowRefresh) {
                    for (doc in snapshot!!.documentChanges) {
                        when (doc.type) {
                            DocumentChange.Type.ADDED -> {
                                cnt++
                                if (cnt > 0) {
                                    Toast.makeText(context, "${cnt}개의 새로운 포스트", Toast.LENGTH_LONG)
                                        .show()
                                }
                                val document = doc.document
                                val post = document.toItem()
                                println("####$$$####" + post.postId)
                                //11-22 여기 추가해야 됨
                                if (post.postId == User.INVALID_USER) {
                                    continue
                                }
                                for (friend in friends) {
                                    if (post.whoPosted == friend)
                                        viewModel.addItem(post)
                                }
                            }
                            DocumentChange.Type.REMOVED -> {

                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("*********************onViewCreated")

        val binding = PostLayoutBinding.bind(view)
        val viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        val db: FirebaseFirestore = Firebase.firestore



        binding.refresh.setOnRefreshListener {
            if (viewModel.itemsSize > viewModel.itemNotified) {
                println("activated222333")
                adapter.notifyItemInserted(viewModel.itemsSize)
            }
            binding.refresh.isRefreshing = false
        }

        //val adapter = MyAdapter(db, navigate, viewModel)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)
    }
}


// 뷰모델 이미지url, 랜덤 아이디 가져오기
class PostingFragment : Fragment(R.layout.posting_layout) {

    private var _binding: PostingLayoutBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: ProfileViewModel

    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore
    val docPostRef = db.collection("post")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PostingLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = PostingLayoutBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        val imgUrl = viewModel.getPos()
        binding.postImage.setImageURI(imgUrl)

        binding.posting.setOnClickListener {

            val comment = binding.comments.text.toString()
            val like = 0
            val whoPosted = "${Firebase.auth.currentUser?.uid}"

            val tampComments = java.util.ArrayList<Map<String, String>>()
            tampComments.add(
                mapOf("${Firebase.auth.currentUser?.uid}" to comment)
            )

            val itemMap = hashMapOf(
                "imgUrl" to imgUrl.toString(),
                "like" to like,
                "whoPosted" to whoPosted,
                "Timestamp" to FieldValue.serverTimestamp(),
                "comments" to tampComments
            )

            docPostRef.add(itemMap)
                .addOnSuccessListener {

                    imgUrl?.let {

                        val imageFile = getRealPathFromURI(it)
                        val imageName = getRealPathFromNAME(it)
                        uploadFile(imageFile, imageName)
                    }
                    Snackbar.make(binding.root, "Upload completed.", Snackbar.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Snackbar.make(binding.root, "Upload fail.", Snackbar.LENGTH_SHORT).show()
                }
        }

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

    // 스토리지에 이미지 업로드
    private fun uploadFile(file_id: Long?, fileName: String?) {
        file_id ?: return
        val imageRef = storage.reference.child("${Firebase.auth.currentUser?.uid}/${fileName}")
        val contentUri =
            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, file_id)
        imageRef.putFile(contentUri).addOnCompleteListener {
            if (it.isSuccessful) {
                // upload success
                Snackbar.make(binding.root, "Upload completed.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 게시물 보여주기
class ProfileFragment : Fragment(R.layout.profile_layout) {
    private var _binding: ProfileLayoutBinding? = null
    private val binding get() = _binding!!

    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore
    val docUserRef = db.collection("Users").document("${Firebase.auth.currentUser?.uid}")
    val docPostRef = db.collection("post").document("${Firebase.auth.currentUser?.uid}")

    val colUserRef = db.collection("Users")
    val colPostRef = db.collection("post")

    lateinit var viewModel: ProfileViewModel

    companion object {
        const val REQ_GALLERY = 1
        const val REQ_PERMISSION_CAMERA = 1
    }

    // 갤러리에서 이미지 선택결과를 받고 뷰모델에 저장
    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageURI = result.data?.data

            viewModel.setPos(imageURI)

        }

        findNavController().navigate(R.id.action_profileFragment_to_postingFragment)

    }

    // 기본 사진앱에서 이미지 선택결과를 받고 뷰모델에 저장ㄹㅇ
    private val photoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageURI = result.data?.data

            viewModel.setPos(imageURI)

        }

        findNavController().navigate(R.id.action_profileFragment_to_postingFragment)
    }

    // 갤러리에서 이미지 선택결과를 받고 프로필화면으로 전환
    private val profileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageURI = result.data?.data
            imageURI?.let {
                binding.profile.setImageURI(imageURI)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ProfileLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ProfileLayoutBinding.bind(view)

        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        storage = Firebase.storage
        val storageRef = storage.reference // reference to root
        val imageRef1 = storageRef.child("상상부기 2.png")


        // 유저 프로필 이미지에서 url 가져와서 띄우기
        docUserRef.get()
            .addOnSuccessListener { // it: DocumentSnapshot
                viewModel.setPro(it["profileImage"].toString())
                val imageRef2 = storage.getReferenceFromUrl(viewModel.getPro().toString())
                displayImageRef(imageRef2, binding.profile)
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

        // 업로드 버튼
        binding.buttonUpload.setOnClickListener {
            //selectGallery()
            AlertDialog.Builder(requireActivity()).apply {
                setTitle("사진촬영 및 갤러리 선택")
                setPositiveButton("Gallery") { _, _ -> selectGallery() }
                setNegativeButton("Photo") { _, _ -> selectPhoto() }

            }.show()

            //findNavController().navigate(R.id.action_profileFragment_to_postingFragment)

        }
        // 프로필 변경 버튼
        binding.buttonProfile.setOnClickListener {
            selectGalleryProfile()
        }

    }


    // 게시물수, 친구수 출력
    private fun queryItem() { // 1번문제
        docUserRef.get()
            .addOnSuccessListener { // it: DocumentSnapshot
                val friend = it["friends"] as ArrayList<*>
                binding.friendNumber.setText(friend.size.toString())
            }.addOnFailureListener {
            }


        colPostRef.whereEqualTo("whoPosted", "${Firebase.auth.currentUser?.uid}").get()
            .addOnSuccessListener { documents ->
                var size = 0
                for (doc in documents) {
                    size++
                }
                binding.postNumber.setText(size.toString())
            }.addOnFailureListener {
            }
    }


    // 기본 사진앱 호출
    private fun selectPhoto() {
        val cameraPermission =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
        val storagePermission =
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )


        if (cameraPermission == PackageManager.PERMISSION_DENIED || storagePermission == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_PERMISSION_CAMERA
            )
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            photoResult.launch(intent)
        }
    }

    //갤러리 호출
    private fun selectGallery() {
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
            // Failed to download the image
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


class CommentFragment : Fragment(R.layout.comment_layout) {

    lateinit var binding: CommentLayoutBinding

    // 댓글 입력창이 나올 때는 바텀넵뷰를 숨긴다. 11-13
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNav(true)


    }

    override fun onDestroy() {
        super.onDestroy()
        val mainActivity = activity as MainActivity
        mainActivity.hideBottomNav(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //AppBarConfiguration(setOf(R.id.commentFragment))
        val binding = CommentLayoutBinding.bind(view)

        binding.commentEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    println("empty")
                    binding.button.isEnabled = false
                } else {
                    println("working")
                    binding.button.isEnabled = true
                }
            }
        })

        binding.button.isEnabled = false

        val viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        //val viewModel = MyViewModel()
        //binding.textView.text = "working"

        binding.backToPost.setOnClickListener {
            //findNavController().navigate(R.id.postFragment)
            findNavController().navigateUp()
        }

        val db: FirebaseFirestore = Firebase.firestore
        var storage = Firebase.storage
        var newComment = ArrayList<Map<String, String>>()

        println("########yellow##########" + viewModel.items.get(viewModel.getPos()).postId)
        val comments = viewModel.getComment(viewModel.getPos())

        val clickedPost = viewModel.items.get(viewModel.getPos())

        val adapter = CommentAdapter(db, comments)
        //binding.button.isEnabled = false

        binding.button.setOnClickListener {
            val comment = binding.commentEdit.text.toString()
            val newCommentMap = mapOf("UXEKfhpQLYnVFXCTFl9P" to comment)
            comments.add(newCommentMap)
            // 여기 .document에 내 uid가 들어가야 된다.
            db.collection("PostInfo").document(viewModel.notifyClickedPostInfo())
                .update(
                    mapOf(
                        "testing" to comments
                    )
                )
            //viewModel.setComments(comments)
            adapter.notifyItemInserted(comments.size - 1)
        }
        //var string: String = "not working"


        val postId = viewModel.items.get(viewModel.getPos()).postId
        binding.commentRecy.adapter = adapter
        binding.commentRecy.layoutManager = LinearLayoutManager(context)

        db.collection("SonUsers").document("UXEKfhpQLYnVFXCTFl9P").get().addOnSuccessListener {
            val temp = it["profileImage"].toString()
            val profileImageRef = storage.getReferenceFromUrl(temp)
            profileImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                binding.profileImg.setImageBitmap(bmp)
            }.addOnFailureListener {}
        }

    }
}
