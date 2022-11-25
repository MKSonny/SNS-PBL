package com.example.firebasestoreandauth.fragments.post

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.adapter.MyAdapter
import com.example.firebasestoreandauth.databinding.FragmentPostMainBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.ProfileViewModel
import com.example.firebasestoreandauth.utils.extentions.toItem
import com.example.firebasestoreandauth.utils.extentions.toUser
import com.example.firebasestoreandauth.utils.getReferenceOfMine
import com.example.firebasestoreandauth.viewmodels.ItemNotify
import com.example.firebasestoreandauth.viewmodels.PostViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostFragment : Fragment(R.layout.fragment_post_main) {
    private var _binding: FragmentPostMainBinding? = null
    val binding get() = _binding!!
    private var snapshotListener: ListenerRegistration? = null
    private var myReference: ListenerRegistration? = null
    private lateinit var adapter: MyAdapter
    private var cnt = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var nowRefresh = false
        val friends = mutableSetOf<String>()
        val db = Firebase.firestore
        val viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)

        val navigate = findNavController()
        adapter = MyAdapter(Firebase.firestore, navigate, viewModel)

        myReference =
            getReferenceOfMine()?.addSnapshotListener { snapshot, err ->
                snapshot?.let {
                    val me = it.toUser()
                    if (me.uid == User.INVALID_USER)
                        return@addSnapshotListener
                    val newFriendList = me.friends?.toMutableList() ?: mutableListOf()
                    friends.clear()
                    friends.addAll(newFriendList)
                    friends.add(me.uid.toString()) // 자기 게시물도 볼 수 있도록
                    snapshotListener?.remove()
                    //viewModel.clearAll() // -> 실시간성 때문에

                    db.collection("PostInfo").orderBy("time", Query.Direction.DESCENDING).get().addOnSuccessListener {
                        for (doc in it) {
                            val post = doc.toItem()
                            for (friend in friends) {
                                if (post.whoPosted == friend) {
                                    viewModel.addItem(post)
                                }
                            }
                            adapter.notifyItemInserted(viewModel.itemNotified)
                            nowRefresh = true
                        }

                        nowRefresh = true
                    }

                    snapshotListener =
                        db.collection("PostInfo").addSnapshotListener { snapshot, error ->
                            if(nowRefresh) {
                                for (doc in snapshot!!.documentChanges) {
                                    when (doc.type) {
                                        DocumentChange.Type.ADDED -> {
                                            val document = doc.document
                                            val post = document.toItem()
                                            if (post.postId == User.INVALID_USER) {
                                                continue
                                            }
                                            for (friend in friends) {
                                                if (post.whoPosted == friend) {
                                                    viewModel.addItem(post)
                                                    //adapter?.notifyItemInserted(viewModel.itemNotified)
                                                    cnt++
                                                }
                                            }
                                            if (cnt > 0) {
                                                Toast.makeText(
                                                    context,
                                                    "${cnt}개의 새로운 포스트",
                                                    Toast.LENGTH_LONG
                                                )
                                                    .show()
                                            }

                                        }
                                        DocumentChange.Type.MODIFIED -> {
                                            val added = doc.document.id
                                            val heart = viewModel.items
                                            for (post in heart) {
                                                if (post.postId == added) {
                                                    post.likes = doc.document["likes"] as Number
                                                    //println("&&&&&&&&&&&"+pos)
                                                    adapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                        DocumentChange.Type.REMOVED -> {

                                        }
                                        else -> {}
                                    }
                                    //adapter?.notifyDataSetChanged()
                                }
                            }
                        }
                }
            }
//        db.collection("PostInfo").orderBy("time", Query.Direction.DESCENDING).get()
//            .addOnSuccessListener {
//                for (doc in it) {
//                    val post = doc.toItem()
//                    for (friend in friends) {
//                        if (post.whoPosted == friend) {
//                            viewModel.addItem(post)
//                            adapter?.notifyItemInserted(viewModel.itemNotified)
//                        }
//                    }
//                    nowRefresh = true
//                }
//                nowRefresh = true
//            }
//        getReferenceOfMine()?.get()?.addOnSuccessListener {
//            val me = it.toUser()
//            if (me.uid == User.INVALID_USER)
//                return@addOnSuccessListener
//            val friends = me.friends?.toMutableList() ?: mutableListOf("")
//            friends.add(me.uid.toString()) // 자기 게시물도 볼 수 있도록
//            db.collection("PostInfo").orderBy("time", Query.Direction.DESCENDING).get()
//                .addOnSuccessListener {
//                    for (doc in it) {
//                        val post = doc.toItem()
//                        for (friend in friends) {
//                            if (post.whoPosted == friend) {
//                                viewModel.addItem(post)
//                                adapter.notifyItemInserted(viewModel.itemNotified)
//                            }
//                        }
//                        nowRefresh = true
//                    }
//                    nowRefresh = true
//                }
//        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        //snapshotListener?.remove()
        //binding.recyclerView.adapter = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        myReference?.remove()
        snapshotListener?.remove()
        binding.recyclerView.adapter = null
        //adapter = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //val viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        _binding = FragmentPostMainBinding.inflate(inflater, container, false)
        //val navigate = findNavController()
        //adapter = MyAdapter(Firebase.firestore, navigate, viewModel)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        binding.refresh.setOnRefreshListener {
            if (viewModel.itemsSize > viewModel.itemNotified) {
                println("#####$$$#####"+viewModel.itemsSize)
                println("#####$$$#####"+viewModel.itemNotified)
                adapter.notifyItemInserted(viewModel.itemsSize)
            }
            binding.refresh.isRefreshing = false
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)
    }
}