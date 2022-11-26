package com.example.firebasestoreandauth.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.ItemPostBinding
import com.example.firebasestoreandauth.dto.Item
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.viewmodels.PostViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class MyAdapter(
    private val db: FirebaseFirestore,
    private val navigate: NavController,
    private val viewModel: PostViewModel
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private object TIME_MAXIMUM {
        const val SEC = 60
        const val MIN = 60
        const val HOUR = 24
        const val DAY = 30
        const val MONTH = 12
    }

    fun formatTimeString(regTime: Long): String? {
        val curTime = System.currentTimeMillis()
        var diffTime = (curTime - regTime) / 1000
        var msg: String? = null
        if (diffTime < TIME_MAXIMUM.SEC) {
            msg = "방금 전"
        } else if (TIME_MAXIMUM.SEC.let { diffTime /= it; diffTime } < TIME_MAXIMUM.MIN) {
            msg = diffTime.toString() + "분 전"
        } else if (TIME_MAXIMUM.MIN.let { diffTime /= it; diffTime } < TIME_MAXIMUM.HOUR) {
            msg = diffTime.toString() + "시간 전"
        } else if (TIME_MAXIMUM.HOUR.let { diffTime /= it; diffTime } < TIME_MAXIMUM.DAY) {
            msg = diffTime.toString() + "일 전"
        } else if (TIME_MAXIMUM.DAY.let { diffTime /= it; diffTime } < TIME_MAXIMUM.MONTH) {
            msg = diffTime.toString() + "달 전"
        } else {
            msg = diffTime.toString() + "년 전"
        }
        return msg
    }

    var storage = Firebase.storage

    inner class ViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setContents(pos: Int) {
            var item = viewModel.items[pos]
            //binding.timeStamp.text = timeDiff(item.time)
            val tempComments = ArrayList<Map<String, String>>()
            tempComments.add(
                mapOf("only" to "friends")
            )

            if (item.time.nanoseconds == 0)
                binding.time.text = "" //binding.time.text = "타임스탬프 오류"
            else
                binding.time.text = formatTimeString(item.time.toDate().time)

//            val tempItemMap = hashMapOf(
//                "comments" to tempComments,
//                "likes" to 0,
//                "img" to "gs://sns-pbl.appspot.com/wine.jpg",
//                "profile_img" to "gs://sns-pbl.appspot.com/상상부기 2.png",
//                "testing" to tempComments,
//                "whoPosted" to "UXEKfhpQLYnVFXCTFl9P"
//            )
            binding.button2.setOnClickListener {
                val forPostId = db.collection("PostInfo").document()
                val tempItemMap2 = hashMapOf(
                    "comments" to tempComments,
                    "likes" to 0,
                    //"img" to "gs://sns-pbl.appspot.com/wine.jpg",
                    //"profile_img" to "gs://sns-pbl.appspot.com/상상부기 2.png",
                    //"time" to FieldValue.serverTimestamp(),
                    "testing" to tempComments,
                    "whoPosted" to "72o26k0KUHfPpZm7vVjViNVJci22",
                    "post_id" to forPostId.id,
                    "time" to Timestamp(0, 0)
                )
                forPostId.set(tempItemMap2)
            }
            val postId = item.postId
            val whoPosted = item.whoPosted
            var likes = item.likes.toInt()
//            println("##$###$$$###timestamp+" + item.time)
//            var profileRef: String
//            var liked = false
            // 프로필 사진 옆 유저 아이디 표시
            db.collection("Users").document(whoPosted)
                .get()
                .addOnSuccessListener {
                    binding.userId.setText(it["nickname"].toString())
                    binding.uid.setText(it["nickname"].toString())
                    val imageURL = it["profileImage"].toString()
                    try {
                        if (imageURL.startsWith("gs:")) {
                            val profileImageRef = storage.getReferenceFromUrl(imageURL)
                            profileImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                                binding.profileImg.setImageBitmap(bmp)
                            }.addOnFailureListener {}
                        }
                    } catch (e:Exception) {

                    }

                }

            // 좋아요 수를 표시
            binding.showLikes.text = "좋아요 " + likes + "개"

            if (viewModel.items[pos].liked) {
                binding.likeBtn.setBackgroundResource(R.drawable.full_heart)
                binding.likeBtn.isSelected = true
                //viewModel.items[pos].liked = false
            }

            binding.likeBtn.setOnClickListener {
                if (it.isSelected) {
                    likes -= 1
                    it.setBackgroundResource(R.drawable.icons8__96)
                    viewModel.items[pos].liked = false
                } else {
                    likes += 1
                    it.setBackgroundResource(R.drawable.full_heart)
                    viewModel.items[pos].liked = true
                    viewModel.items[pos].likes = likes
                }
                it.isSelected = !it.isSelected
                db.collection("PostInfo").document(postId).update("likes", likes)
                binding.showLikes.text = "좋아요 " + likes + "개"
            }

            binding.commentBtn.setOnClickListener {
                //viewModel.setUser(postedUser)
                //viewModel.setPostId(postedUser)
                viewModel.setPos(pos)
                //println("#$#$#$$#setpos" + viewModel.getPos())
                viewModel.ClickedPostInfo(item.postId)
                navigate.navigate(R.id.action_postFragment_to_commentFragment)
            }

            var commentsTest = ArrayList<Map<String, String>>()
            commentsTest.add(mapOf("test" to "hello"))

//            val itemMap = hashMapOf(
//                "comments" to commentsTest,
//                "img" to "gs://sns-pbl.appspot.com/상상부기 2.png",
//                "likes" to 1 as Number,
//                "whoPosted" to "testing"
//            )
//            binding.uid.text = item.whoPosted + " "
//            binding.postTitle.text = item.comments[0][whoPosted]
            binding.postTitle.text = item.comments[0][whoPosted]


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
        val binding = ItemPostBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContents(position)
    }

    override fun getItemCount() = viewModel.itemsSize
}