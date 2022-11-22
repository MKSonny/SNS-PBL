package com.example.firebasestoreandauth

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.*
import com.example.firebasestoreandauth.test.SearchFriendActivity
import com.example.firebasestoreandauth.wrapper.ProfileViewModel
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.example.firebasestoreandauth.wrapper.toUser
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class PostFragment : Fragment(R.layout.post_layout) {
    val db: FirebaseFirestore = Firebase.firestore
    private var snapshotListener: ListenerRegistration? = null
    lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        val navigate = findNavController()

        adapter = MyAdapter(db, navigate, viewModel)

        snapshotListener = db.collection("PostInfo").addSnapshotListener { snapshot, error ->
            for (doc in snapshot!!.documentChanges) {
                when (doc.type) {
                    DocumentChange.Type.ADDED -> {
                       /* val document = doc.document
                        val uid = doc.document.id
                        val profile_img = document["profile_img"] as String
                        val imgUrl = document["img"] as String
                        val likes = document["likes"] as Number
                        //val time = document["time"] as Timestamp
                        val whoPosted = document["whoPosted"] as String
                        val comments = document["testing"] as ArrayList<Map<String,String>>

                        //viewModel.addItem(Item(profile_img, uid, imgUrl, likes, time, whoPosted, comments))
                        //viewModel.addItem(Item(uid, imgUrl, likes, time, whoPosted, comments))
                        //adapter.notifyItemInserted(viewModel.itemNotified)*/
                    }
                    DocumentChange.Type.REMOVED -> {

                    }
                    else -> {}
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
        //val viewModel: MyViewModel by viewModels()
        val db: FirebaseFirestore = Firebase.firestore



        binding.refresh.setOnRefreshListener {
            adapter.notifyItemInserted(viewModel.itemNotified)
            //snapshotListener?.remove()
            binding.refresh.isRefreshing=false
        }

        //val adapter = MyAdapter(db, navigate, viewModel)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)
    }
}

class PostingFragment : Fragment(R.layout.posting_layout){
    private var _binding: PostingLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore
    val docPostRef = db.collection("post").document("${Firebase.auth.currentUser?.uid}")

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

        binding.posting.setOnClickListener {

            val comment = binding.comments.text.toString()
            val like = 0
            val post_id = "${Firebase.auth.currentUser?.uid}"
            val imgUrl = viewModel.getPos()


            val tampComments = java.util.ArrayList<Map<String, String>>()
            tampComments.add(
                mapOf("${Firebase.auth.currentUser?.uid}" to comment)
            )


            val itemMap = hashMapOf(
                "imgUrl" to imgUrl,
                "like" to like,
                "post_id" to post_id,
                "Timestamp" to FieldValue.serverTimestamp(),
                "comments" to tampComments
            )

            docPostRef.set(itemMap)
                .addOnSuccessListener {
                    imgUrl?.let{

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
        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, file_id)
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


class ProfileFragment : Fragment(R.layout.profile_layout) {
    private var _binding: ProfileLayoutBinding? = null
    private val binding get() = _binding!!

    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore
    private var fileAbsolutePath: String? = null
    val docUserRef = db.collection("user").document("${Firebase.auth.currentUser?.uid}")
    val docPostRef = db.collection("post").document("${Firebase.auth.currentUser?.uid}")
    val REQUEST_IMAGE_CAPTURE = 1

    private lateinit var viewModel: ProfileViewModel

    companion object {
        const val REQUEST_CODE = 1
        const val REQ_GALLERY = 1
        const val REQ_PERMISSION_CAMERA = 1
        const val REQ_CAMERA = 1
    }

    // 갤러리에서 이미지 선택결과를 받고 파일 업로드
    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if(result.resultCode == AppCompatActivity.RESULT_OK){
            val imageURI = result.data?.data

            //viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]

            viewModel.setPos(imageURI)

        }
    }

    // 기본 사진앱에서 이미지 선택결과를 받고 파일 업로드
    private val photoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if(result.resultCode == AppCompatActivity.RESULT_OK){
            val imageURI = result.data?.data

            viewModel.setPos(imageURI)

        }
    }

    // 갤러리에서 이미지 선택결과를 받고 프로필화면으로 전환
    private val profileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if(result.resultCode == AppCompatActivity.RESULT_OK){
            val imageURI = result.data?.data
            imageURI?.let{
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

        storage = Firebase.storage
        val storageRef = storage.reference // reference to root
        val imageRef1 = storageRef.child("${Firebase.auth.currentUser?.uid}/hansung2.png")


        // 초기 프로필
        displayImageRef(imageRef1, binding.profile)

        /*// 나중에 자기 포스트에서 이미지 받아오기
        val postRef1 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post1.png"
        )
        val postRef2 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post2.png"
        )
        val postRef3 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post3.png"
        )
        val postRef4 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post4.png"
        )
        val postRef5 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post5.png"
        )
        val postRef6 = storage.getReferenceFromUrl(
            "gs://android-sns-5a992.appspot.com/${Firebase.auth.currentUser?.uid}/post6.png"
        )

        displayImageRef(postRef1, binding.imageView)
        displayImageRef(postRef2, binding.imageView2)
        displayImageRef(postRef3, binding.imageView3)
        displayImageRef(postRef4, binding.imageView4)
        displayImageRef(postRef5, binding.imageView5)
        displayImageRef(postRef6, binding.imageView6)*/

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

            /*supportFragmentManager.commit { // this: FragmentTransaction
                setReorderingAllowed(true)
                replace(R.id.posting_fragment, PostingFragment::class.java, null)
                addToBackStack(null)
            }*/

        }
        // 프로필 변경 버튼
        binding.buttonProfile.setOnClickListener {
            selectGalleryProfile()
        }

    }

    // 기본 사진앱 호출
    private fun selectPhoto(){
        val cameraPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
        val storagePermission =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)


        if (cameraPermission == PackageManager.PERMISSION_DENIED || storagePermission == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQ_PERMISSION_CAMERA)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            photoResult.launch(intent)
        }
    }



    //갤러리 호출
    private fun selectGallery(){
        val writePermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)

        if(writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQ_GALLERY)
        }else{
            val intent = Intent(Intent.ACTION_PICK)

            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )

            imageResult.launch(intent)
        }
    }

    //갤러리 호출 후 프로필사진 변경
    private fun selectGalleryProfile(){
        val writePermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)

        if(writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQ_GALLERY)
        }else{
            val intent = Intent(Intent.ACTION_PICK)

            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )

            profileResult.launch(intent)
        }
    }


    // 게시물수, 친구수 출력
    private fun queryItem() {
        docUserRef.get()
            .addOnSuccessListener { // it: DocumentSnapshot
                binding.friendNumber.setText(it["index"].toString())
            }.addOnFailureListener {
            }

        docPostRef.get()
            .addOnSuccessListener { // it: DocumentSnapshot
                binding.postNumber.setText(it["index"].toString())
            }.addOnFailureListener {
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

class FriendsFragment : Fragment(R.layout.friends_layout) {
    var snapshotListener: ListenerRegistration? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FriendsLayoutBinding.bind(view)
        val friendModel: FriendViewModel by viewModels()
        val listAdapter = FriendListAdapter(friendModel)
        val requestAdapter = RequestReceivedAdapter(friendModel)

        binding.recyclerFriendList.adapter = listAdapter
        binding.recyclerFriendList.layoutManager = LinearLayoutManager(context)
        binding.recyclerFriendList.setHasFixedSize(true)

        binding.recyclerReceivedList.adapter = requestAdapter
        binding.recyclerReceivedList.layoutManager = LinearLayoutManager(context)
        binding.recyclerReceivedList.setHasFixedSize(true)

        friendModel.friend.observe(viewLifecycleOwner) {
            listAdapter.notifyDataSetChanged()
        }
        friendModel.requestReceived.observe(viewLifecycleOwner) {
            requestAdapter.notifyDataSetChanged()
        }

        snapshotListener = getReferenceOfMine()?.addSnapshotListener { snapshot, e ->
            val TAG = "SnapshotListener"
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toUser()
                if (user.UID == User.INVALID_USER) return@addSnapshotListener
                Log.d(TAG, "Current data: ${user}")
                friendModel.friend.setList(user.friends!!)
                friendModel.requestReceived.setList(user.requestReceived!!.toList())
            } else {
                Log.d(TAG, "Current data: null")
            }
        }

        binding.startFindFriendButton.setOnClickListener {
            val intent = Intent(activity, SearchFriendActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
    }
}

class CommentFragment : Fragment(R.layout.comment_layout) {

    lateinit var binding: CommentLayoutBinding

    // 댓글 입력창이 나올 때는 바텀넵뷰를 숨긴다. 11-13
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainActivity = activity as MainActivity
        mainActivity.HideBottomNav(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        val mainActivity = activity as MainActivity
        mainActivity.HideBottomNav(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //AppBarConfiguration(setOf(R.id.commentFragment))
        val binding = CommentLayoutBinding.bind(view)
        val viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        //val viewModel = MyViewModel()
        //binding.textView.text = "working"

        val db: FirebaseFirestore = Firebase.firestore
        var storage = Firebase.storage
        var newComment = ArrayList<Map<String, String>>()

        println("########yellow##########" + viewModel.items.get(viewModel.getPos()).postId)

        //comment 새로 추가하면 바로 보이는 거 수정해야됨
//        db.collection("PostInfo").document(viewModel.items.get(viewModel.getPos()).postId)
//            .addSnapshotListener {
//                    snapshot, error ->
//                if (snapshot != null && snapshot.exists()) {
//                    val temp = snapshot.data!!["comments"] as ArrayList<Map<String, String>>
//                    newComment.add(temp.get(0))
//                    println("#############red###########" + newComment.get(0))
//                }
//            }
        val comments = viewModel.getComment(viewModel.getPos())

        val adapter = CommentAdapter(db, comments)

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
//        db.collection("PostInfo").document(postId)
//            .addSnapshotListener {
//                    snapshot, error ->
//                if ((snapshot != null) && snapshot.exists()) {
//                    val temp = snapshot.data!!["comments"] as ArrayList<Map<String, String>>
//                    viewModel.setComments(temp)
//                    adapter.notifyItemInserted(viewModel.itemNotified)              }
//            }

        binding.commentRecy.adapter = adapter
        binding.commentRecy.layoutManager = LinearLayoutManager(context)
    }
}
