package com.example.firebasestoreandauth

import android.app.ActionBar
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.databinding.PostLayoutBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class PostFragment : Fragment(R.layout.post_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = PostLayoutBinding.bind(view)

        val viewModel: MyViewModel by viewModels()
        //binding.textView.text = "working"

        val db: FirebaseFirestore = Firebase.firestore

        //val nhf = parentFragmentManager.findFragmentById(R.id.fragments)

        val navigate = findNavController()

        val adapter = MyAdapter(db, navigate, viewModel)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)

        // document id로 검색하는 걸 로 수정
        db.collection("PostInfo").orderBy("time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                    result ->
                for(document in result) {
                    val uid = document.id
                    val imgUrl = document["img"] as String
                    val likes = document["likes"] as Number
                    val time = document["time"] as Timestamp
                    viewModel.addItem(Item(uid, imgUrl, likes, time))
                    //viewModel.updateItem(Item(name, imgUrl), viewModel.itemsSize)
                }
            }
            .addOnFailureListener {

            }

        // observe 함수를 adapter 밑에서 구현
        // 맨위로 끌어올릴 경우 호출되도록? observer pattern 적용
        viewModel.itemLiveData.observe(viewLifecycleOwner) {
            // 전체를 다 바꿔줌으로 비효율적
            // 추가된 부분만 업데이트 될수록 수정 필요
            //adapter.notifyDataSetChanged()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

class CommentFragment : Fragment(R.layout.comment_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppBarConfiguration(setOf(R.id.commentFragment))
    }
}