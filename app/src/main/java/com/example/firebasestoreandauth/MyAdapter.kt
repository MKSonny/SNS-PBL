package com.example.firebasestoreandauth

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.databinding.ItemLayoutBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.time.Duration.Companion.nanoseconds


class MyAdapter(private val db: FirebaseFirestore, private val navigate: NavController, private val viewModel: MyViewModel) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    enum class TimeValue(val value: Int,val maximum : Int, val msg : String) {
        SEC(60,60,"분 전"),
        MIN(60,24,"시간 전"),
        HOUR(24,30,"일 전"),
        DAY(30,12,"달 전"),
        MONTH(12,Int.MAX_VALUE,"년 전")
    }

    //var curTime = System.currentTimeMillis()

    fun timeDiff(timestamp: Timestamp): String? {
        val curTime = System.currentTimeMillis()
        curTime.nanoseconds
        var diffTime = (curTime - timestamp.nanoseconds) / 1000
        var msg: String? = null
        if(diffTime < TimeValue.SEC.value )
            msg= "방금 전"
        else {
            for (i in TimeValue.values()) {
                diffTime /= i.value
                if (diffTime < i.maximum) {
                    msg=i.msg
                    break
                }
            }
        }
        return msg
    }

    var storage = Firebase.storage

    inner class ViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setContents(pos: Int) {
            var item = viewModel.items[pos]
            //binding.timeStamp.text = timeDiff(item.time)
            val tempComments = ArrayList<Map<String, String>>()
            tempComments.add(
                mapOf("only" to "friends")
            )
            val tempItemMap = hashMapOf(
                "comments" to tempComments,
                "likes" to 0,
                "img" to "gs://sns-pbl.appspot.com/wine.jpg",
                "profile_img" to "gs://sns-pbl.appspot.com/상상부기 2.png",
                "testing" to tempComments,
                "whoPosted" to "UXEKfhpQLYnVFXCTFl9P"
            )
            binding.button2.setOnClickListener {
                val forPostId = db.collection("PostInfo").document()
                val tempItemMap2 = hashMapOf(
                    "comments" to tempComments,
                    "likes" to 0,
                    //"img" to "gs://sns-pbl.appspot.com/wine.jpg",
                    //"profile_img" to "gs://sns-pbl.appspot.com/상상부기 2.png",
                    "testing" to tempComments,
                    "whoPosted" to "odcYUEo7Mhbhmbc13Xzm",
                    "post_id" to forPostId.id
                )
                forPostId.set(tempItemMap2)
            }
            val postId = item.postId
            val whoPosted = item.whoPosted
            var likes = item.likes.toInt()
            //println("##$###$$$###timestamp+" + item.time)
            var profileRef: String
            var liked = false
            // 프로필 사진 옆 유저 아이디 표시
            db.collection("SonUsers").document(whoPosted)
                .get()
                .addOnSuccessListener {
                    binding.userId.setText(it["nickName"].toString())
                    binding.uid.setText(it["nickName"].toString())
                    val profileImageRef = storage.getReferenceFromUrl(it["profileImage"].toString())

                    profileImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                        val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                        binding.profileImg.setImageBitmap(bmp)
                    }.addOnFailureListener {}
                }

            // 좋아요 수를 표시
            binding.showLikes.text = "좋아요 " + likes + "개"

            binding.likeBtn.setOnClickListener {
                if (it.isSelected) {
                    likes -= 1
                    it.setBackgroundResource(R.drawable.icons8__96)
                }
                else {
                    likes += 1
                    viewModel.items[pos].likes = likes
                    it.setBackgroundResource(R.drawable.full_heart)
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

            val itemMap = hashMapOf(
                "comments" to commentsTest,
                "img" to "gs://sns-pbl.appspot.com/상상부기 2.png",
                "likes" to 1 as Number,
                "whoPosted" to "testing"
            )


            //binding.uid.text = item.whoPosted + " "
            //binding.postTitle.text = item.comments[0][whoPosted]
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
        val binding = ItemLayoutBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContents(position)
    }

    override fun getItemCount() = viewModel.itemsSize
}