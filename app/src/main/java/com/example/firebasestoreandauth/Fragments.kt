package com.example.firebasestoreandauth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.databinding.CommentLayoutBinding
import com.example.firebasestoreandauth.databinding.PostLayoutBinding
import com.google.firebase.Timestamp
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        val navigate = findNavController()

        adapter = MyAdapter(db, navigate, viewModel)

        snapshotListener = db.collection("PostInfo").addSnapshotListener { snapshot, error ->
            for (doc in snapshot!!.documentChanges) {
                when (doc.type) {
                    DocumentChange.Type.ADDED -> {
                        val refresh = doc.document
                        val uid = doc.document.id
                        val imgUrl = refresh["img"] as String
                        //val likes = refresh["likes"] as Number
                        //val time = refresh["time"] as Timestamp
                        val whoPosted = refresh["whoPosted"] as String
                        val comments = refresh["comments"] as ArrayList<Map<String,String>>

                        println("################modifed")
                        viewModel.addItem(Item(uid, imgUrl, whoPosted, comments))
                        //viewModel.addItem(Item(uid, imgUrl, likes, time, whoPosted, comments))
                        //adapter.notifyItemInserted(viewModel.itemNotified)
                    }
                    DocumentChange.Type.REMOVED -> {

                    }
                    else -> {}
                }
            }
        }

        // document id로 검색하는 걸 로 수정
//        db.collection("PostInfo")//.orderBy("time", Query.Direction.DESCENDING)
//            .get()
//            .addOnSuccessListener {
//                    result ->
//                for(document in result) {
//                    val uid = document.id
//                    val imgUrl = document["img"] as String
//                    //val likes = document["likes"] as Number
//                    //val time = document["time"] as Timestamp
//                    val whoPosted = document["whoPosted"] as String
//                    val comments = document["comments"] as ArrayList<Map<String,String>>
//
//                    viewModel.addItem(Item(uid, imgUrl, whoPosted, comments))
//                    //viewModel.addItem(Item(uid, imgUrl, likes, time, whoPosted, comments))
//                    //viewModel.updateItem(Item(name, imgUrl), viewModel.itemsSize)
//                    adapter.notifyItemInserted(viewModel.itemNotified)
//                }
//            }
//            .addOnFailureListener {
//
//            }

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

//        viewModel.itemLiveData.observe(viewLifecycleOwner) {
//            // 전체를 다 바꿔줌으로 비효율적
//            // 추가된 부분만 업데이트 될수록 수정 필요
//            //adapter.notifyDataSetChanged()
//            when (viewModel.itemNotifiedType) {
//                ItemNotify.ADD -> adapter.notifyItemInserted(viewModel.itemNotified)
//                ItemNotify.UPDATE -> adapter.notifyItemChanged(viewModel.itemNotified)
//                ItemNotify.DELETE -> adapter.notifyItemRemoved(viewModel.itemNotified)
//            }
//        }

        //val nhf = parentFragmentManager.findFragmentById(R.id.fragments)
        // val viewModel: MyViewModel by viewModels()
        //binding.textView.text = "working"

        //val db: FirebaseFirestore = Firebase.firestore
//        val navigate = findNavController()







        // observe 함수를 adapter 밑에서 구현
        // 맨위로 끌어올릴 경우 호출되도록? observer pattern 적용


    }
}

class ProfileFragment : Fragment(R.layout.profile_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

class FriendsFragment : Fragment(R.layout.friends_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        //AppBarConfiguration(setOf(R.id.))

        binding = CommentLayoutBinding.bind(view)



        val viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        //val viewModel = MyViewModel()
        //binding.textView.text = "working"

        val db: FirebaseFirestore = Firebase.firestore

        //var string: String = "not working"

        val adapter = CommentAdapter(db, viewModel.getComment(viewModel.getPos()))

        binding.commentRecy.adapter = adapter
        binding.commentRecy.layoutManager = LinearLayoutManager(context)
        binding.commentRecy.setHasFixedSize(true)

        // observe 함수를 adapter 밑에서 구현
        // 맨위로 끌어올릴 경우 호출되도록? observer pattern 적용
    }
}