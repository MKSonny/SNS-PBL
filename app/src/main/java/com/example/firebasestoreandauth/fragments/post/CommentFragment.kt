package com.example.firebasestoreandauth.fragments.post


import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.MainActivity
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.adapter.CommentAdapter
import com.example.firebasestoreandauth.databinding.FragmentPostCommentBinding
import com.example.firebasestoreandauth.viewmodels.PostViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class CommentFragment : Fragment(R.layout.fragment_post_comment) {

    lateinit var binding: FragmentPostCommentBinding
    val myId = Firebase.auth.uid ?: "null"
    lateinit var myNickName: String

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
        val binding = FragmentPostCommentBinding.bind(view)

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

        val viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)
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

//        getReferenceOfMine()?.addSnapshotListener { snapshot, err ->
//            snapshot?.let {
//                myNickName = it["nickname"] as String
//            }
//        }

        binding.button.setOnClickListener {
            val comment = binding.commentEdit.text.toString()
            val newCommentMap = mapOf(myId to comment)
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
            binding.commentEdit.text.clear()
        }
        //var string: String = "not working"

        val postId = viewModel.items.get(viewModel.getPos()).postId
        binding.commentRecy.adapter = adapter
        binding.commentRecy.layoutManager = LinearLayoutManager(context)
        if (myId != null) {
            db.collection("Users").document(myId).get().addOnSuccessListener {
                try {
                    val temp = it["profileImage"].toString()
                    val profileImageRef = storage.getReferenceFromUrl(temp)
                    profileImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                        val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                        binding.profileImg.setImageBitmap(bmp)
                    }.addOnFailureListener {}
                } catch (e: Exception) {
                    binding.profileImg.setImageResource(android.R.color.transparent)
                }
            }
        }
    }
}
