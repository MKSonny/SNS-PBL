package com.example.firebasestoreandauth

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.databinding.ItemLayoutBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MyAdapter(private val db: FirebaseFirestore, private val navigate: NavController, private val viewModel: MyViewModel) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    var storage = Firebase.storage

    inner class ViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setContents(pos: Int) {
            val item = viewModel.items[pos]
            val postedUser = item.uid
            var likes = item.likes.toInt()

            // 프로필 사진 옆 유저 아이디 표시
            binding.userId.text = postedUser
            // 좋아요 수를 표시
            binding.showLikes.text = "좋아요 " + likes + "개"

            binding.likeBtn.setOnClickListener {
                likes++
                db.collection("PostInfo").document(postedUser).update("likes", likes)
                binding.showLikes.text = "좋아요 " + likes + "개"
            }

            binding.commentBtn.setOnClickListener {
                //viewModel.setUser(postedUser)
                //viewModel.setPostId(postedUser)
                viewModel.setPos(pos)
                //println("#$#$#$$#setpos" + viewModel.getPos())
                navigate.navigate(R.id.action_postFragment_to_commentFragment)
            }

            binding.commentSend.setOnClickListener {
                //val msg = binding.commentBox.text.toString()
                //db.collection("PostInfo").document(postedUser).update("comments", FieldValue.arrayUnion(msg))
            }

            val imageRef = storage.getReferenceFromUrl(item.postImgUrl)

            imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                binding.postImg.setImageBitmap(bmp)
            }.addOnFailureListener {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLayoutBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContents(position)
    }

    override fun getItemCount() = viewModel.itemsSize
}