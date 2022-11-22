package com.example.firebasestoreandauth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.CommentLayoutBinding
import com.example.firebasestoreandauth.databinding.PostLayoutBinding
import com.example.firebasestoreandauth.wrapper.toItem
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


        //}
        //for (it in friends)
        println("adfadfadfadfadf444#$$" + friends.size)

        var nowRefresh = false
    db.collection("SonUsers").document("UXEKfhpQLYnVFXCTFl9P").get().addOnSuccessListener {
        val friends = it["friends"] as ArrayList<String>
        db.collection("PostInfo").get().addOnSuccessListener {
            for (doc in it) {
                val post = doc.toItem()
                for (friend in friends) {
                    if (post.whoPosted == friend)
                        viewModel.addItem(post)
                }
                adapter.notifyItemInserted(viewModel.itemNotified)
            }
            nowRefresh = true
        }
        snapshotListener = db.collection("PostInfo").addSnapshotListener { snapshot, error ->
            if (nowRefresh) {
                for (doc in snapshot!!.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            val document = doc.document
                            val post = document.toItem()
                            println("####$$$####" + post.postId)
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
        binding.commentRecy.adapter = adapter
        binding.commentRecy.layoutManager = LinearLayoutManager(context)
    }
}