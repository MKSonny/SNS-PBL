package com.example.firebasestoreandauth.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
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
import kotlinx.coroutines.delay


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
    private val differ = AsyncListDiffer(this, MyAdapter.differCallback)

    inner class ViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setContents(pos: Int) {
            var item = differ.currentList[adapterPosition]
            //binding.timeStamp.text = timeDiff(item.time)
            val tempComments = ArrayList<Map<String, String>>()
            tempComments.add(
                mapOf("only" to "friends")
            )
            if (item.time.nanoseconds == 0)
                binding.time.text = formatTimeString(System.currentTimeMillis())//binding.time.text = "타임스탬프 오류"
            else
                binding.time.text = formatTimeString(item.time.toDate().time)
            binding.button2.visibility = View.GONE

            binding.button2.setOnClickListener {
                val forPostId = db.collection("PostInfo").document()
                val tempItemMap2 = hashMapOf(
                    "comments" to tempComments,
                    "likes" to 0,
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

            if (item.liked) {
                binding.likeBtn.setBackgroundResource(R.drawable.full_heart)
                binding.likeBtn.isSelected = true
            } else {
                binding.likeBtn.setBackgroundResource(R.drawable.icons8__96)
                binding.likeBtn.isSelected = false
            }

            binding.likeBtn.setOnClickListener {
                if (it.isSelected) {
                    likes -= 1
                    it.setBackgroundResource(R.drawable.icons8__96)
                    item.liked = false
                } else {
                    likes += 1
                    it.setBackgroundResource(R.drawable.full_heart)
                    item.liked = true
                    item.likes = likes
                }
                it.isSelected = !it.isSelected
                db.collection("PostInfo").document(postId).update("likes", likes)
                binding.showLikes.text = "좋아요 " + likes + "개"
            }

            binding.commentBtn.setOnClickListener {
                viewModel.setPos(adapterPosition)
                viewModel.ClickedPostInfo(item.postId)
                navigate.navigate(R.id.action_postFragment_to_commentFragment)
            }

            var commentsTest = ArrayList<Map<String, String>>()
            commentsTest.add(mapOf("test" to "hello"))

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

    override fun getItemCount() = differ.currentList.size

    fun submitList(list: List<Item>) {
        val x = mutableListOf<Item>().apply {
            addAll(list)
        }
        differ.submitList(x.toList())
    }

    companion object {
        val differCallback = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.whoPosted == newItem.whoPosted
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }
}