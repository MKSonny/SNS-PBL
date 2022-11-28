package com.example.firebasestoreandauth.fragments.post

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.adapter.MyAdapter
import com.example.firebasestoreandauth.databinding.FragmentPostMainBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.extentions.toItem
import com.example.firebasestoreandauth.utils.extentions.toUser
import com.example.firebasestoreandauth.utils.getReferenceOfMine
import com.example.firebasestoreandauth.viewmodels.PostViewModel
import com.google.firebase.firestore.DocumentChange
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
    private var nowRefresh = false
    private val friends = mutableSetOf<String>()
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        val navigate = findNavController()
        adapter = MyAdapter(Firebase.firestore, navigate, viewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        myReference?.remove()
        snapshotListener?.remove()
        binding.recyclerView.adapter = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostMainBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
        if (myReference == null)
            attachSnapshotListener()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        viewModel.itemLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList())
        }
        binding.refresh.setOnRefreshListener {
            if (viewModel.itemsSize > viewModel.itemNotified) {
                println("#####$$$#####" + viewModel.itemsSize)
                println("#####$$$#####" + viewModel.itemNotified)
            }
            binding.refresh.isRefreshing = false
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun attachSnapshotListener() {
        val viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
        getReferenceOfMine()?.addSnapshotListener { snapshot, err ->
            snapshot?.let {
                val me = it.toUser()
                if (me.uid == User.INVALID_USER)
                    return@addSnapshotListener
                val newFriendList = me.friends?.toMutableList() ?: mutableListOf()
                friends.clear()
                friends.addAll(newFriendList)
                friends.add(me.uid.toString()) // 자기 게시물도 볼 수 있도록
                //snapshotListener?.remove() 11-27 수정 오후 9시, 있으면 시간 순서 꼬여요
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

                if (snapshotListener == null)
                    snapshotListener =
                        db.collection("PostInfo").addSnapshotListener { snapshot, error ->
                            if (nowRefresh) {
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
                                                    viewModel.addToFirst(post)
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
                                            val modified = doc.document.toItem()
                                            viewModel.addItem(modified)
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
    }
}