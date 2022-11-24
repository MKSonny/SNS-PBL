package com.example.firebasestoreandauth.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.MyAdapter
import com.example.firebasestoreandauth.MyViewModel
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.PostLayoutBinding
import com.example.firebasestoreandauth.wrapper.toItem
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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