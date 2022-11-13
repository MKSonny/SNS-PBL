package com.example.firebasestoreandauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.CommentLayoutBinding
import com.example.firebasestoreandauth.databinding.FriendsLayoutBinding
import com.example.firebasestoreandauth.databinding.PostLayoutBinding
import com.example.firebasestoreandauth.test.AddPersonalInfoActivity
import com.example.firebasestoreandauth.test.SearchFriendActivity
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.example.firebasestoreandauth.wrapper.toUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostFragment : Fragment(R.layout.post_layout) {
    val db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MyViewModel by viewModels()
        // document id로 검색하는 걸 로 수정
        db.collection("PostInfo").orderBy("time", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val uid = document.id
                    val imgUrl = document["img"] as String
                    val likes = document["likes"] as Number
                    val time = document["time"] as Timestamp
//                    val comments = document["comments"] as Map<String, String>
                    val comments = mapOf("Lee" to "", "Son" to "")
                    viewModel.addItem(Item(uid, imgUrl, likes, time, comments))
                }
            }.addOnFailureListener {

            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = PostLayoutBinding.bind(view)

        val viewModel: MyViewModel by viewModels()
        val adapter = MyAdapter(db, viewModel)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)

        viewModel.itemLiveData.observe(viewLifecycleOwner) {
            // 전체를 다 바꿔줌으로 비효율적
            // 추가된 부분만 업데이트 될수록 수정 필요
            when (viewModel.itemNotifiedType) {
                ItemNotify.ADD -> adapter.notifyItemInserted(viewModel.itemNotified)
                ItemNotify.UPDATE -> adapter.notifyItemChanged(viewModel.itemNotified)
                ItemNotify.DELETE -> adapter.notifyItemRemoved(viewModel.itemNotified)
            }
        }


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //AppBarConfiguration(setOf(R.id.commentFragment))

        val binding = CommentLayoutBinding.bind(view)

        val viewModel: MyViewModel by viewModels()
        //binding.textView.text = "working"

        val db: FirebaseFirestore = Firebase.firestore

        //val nhf = parentFragmentManager.findFragmentById(R.id.fragments)

        val navigate = findNavController()

        val adapter = CommentAdapter(db, navigate, viewModel)

        binding.commentRecy.adapter = adapter
        //binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.commentRecy.setHasFixedSize(true)

        // observe 함수를 adapter 밑에서 구현
        // 맨위로 끌어올릴 경우 호출되도록? observer pattern 적용


    }
}