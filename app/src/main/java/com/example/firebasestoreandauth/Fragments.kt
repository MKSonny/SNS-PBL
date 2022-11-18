package com.example.firebasestoreandauth

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.CommentLayoutBinding
import com.example.firebasestoreandauth.databinding.FriendsLayoutBinding
import com.example.firebasestoreandauth.databinding.PostLayoutBinding
import com.example.firebasestoreandauth.test.SearchFriendActivity
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.example.firebasestoreandauth.wrapper.toUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PostFragment : Fragment(R.layout.post_layout) {
    val db: FirebaseFirestore = Firebase.firestore
    private var snapshotListener: ListenerRegistration? = null
    lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        val navigate = findNavController()

        adapter = MyAdapter(db, navigate, viewModel)

        var friends = ArrayList<String>()
        // 로그인 후 나의 문서 코드를 document 안에 수정합니다.
        db.collection("SonUsers").document("UXEKfhpQLYnVFXCTFl9P").get().addOnSuccessListener {
            val friends2 = it["friends"] as ArrayList<String>
            for (str in friends2) {
                val temp = str
                friends.add(temp)
            }
        }
        //for (it in friends)
        println("adfadfadfadfadf444#$$"+friends.size)

        var nowRefresh = false
        db.collection("PostInfo").get().addOnSuccessListener {
            for (doc in it) {
                val uid = doc.id
                val profile_img = doc["profile_img"] as String
                val imgUrl = doc["img"] as String
                val likes = doc["likes"] as Number
                val time = doc["time"] as Timestamp
                val whoPosted = doc["whoPosted"] as String
                val comments = doc["testing"] as ArrayList<Map<String,String>>

                viewModel.addItem(Item(profile_img, uid, imgUrl, likes, time, whoPosted, comments))
                adapter.notifyItemInserted(viewModel.itemNotified)
            }
        }
        snapshotListener = db.collection("PostInfo").addSnapshotListener { snapshot, error ->

            for (doc in snapshot!!.documentChanges) {
                when (doc.type) {
                    DocumentChange.Type.ADDED -> {
                        val document = doc.document
                        val whoPosted = document["whoPosted"] as String
                        //for (it in friends) {
                        //    println("adfadfadfadfadf#$$"+it)
                         //   if ( it == whoPosted) {
//                                val uid = doc.document.id
//                                val profile_img = document["profile_img"] as String
//                                val imgUrl = document["img"] as String
//                                val likes = document["likes"] as Number
//                                val time = document["time"] as Timestamp
//                                val comments = document["testing"] as ArrayList<Map<String,String>>
//
//                                viewModel.addItem(Item(profile_img, uid, imgUrl, likes, time, whoPosted, comments))
                         //   }
                        //}
                        //viewModel.addItem(Item(uid, imgUrl, likes, time, whoPosted, comments))
                        //adapter.notifyItemInserted(viewModel.itemNotified)
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
            //adapter.notifyItemInserted(viewModel.itemNotified)
            //snapshotListener?.remove()
            binding.refresh.isRefreshing=false
        }

        //val adapter = MyAdapter(db, navigate, viewModel)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)
    }
}

class ProfileFragment : Fragment(R.layout.profile_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                .update(mapOf(
                    "testing" to comments
                ))
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