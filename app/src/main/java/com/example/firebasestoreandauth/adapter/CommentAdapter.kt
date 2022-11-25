package com.example.firebasestoreandauth.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.databinding.ItemPostCommentBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class CommentAdapter(
    private val db: FirebaseFirestore,
    private val comments: ArrayList<Map<String, String>>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    val storage = Firebase.storage
    var moreThan = false

    inner class ViewHolder(private val binding: ItemPostCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setContents(pos: Int) {
            db.collection("Users")
                .document(comments[pos].keys.toString().replace("[", "").replace("]", ""))
                .get().addOnSuccessListener {

                    binding.commentId.setText(it["nickname"].toString())
                    val profile_img = it["profileImage"].toString()

                    try {
                        if(profile_img.startsWith("gs")){
                        val profileImageRef = storage.getReferenceFromUrl(profile_img)

                        profileImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                            binding.profileImg.setImageBitmap(bmp)
                        }.addOnFailureListener {} }
                    } catch (e:Exception) {}

                }
            //binding.commentId.text = comments[pos].keys.toString().replace("[","").replace("]","")
            binding.commentText.text =
                comments[pos].values.toString().replace("[", "").replace("]", "")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPostCommentBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContents(position)
    }

    override fun getItemCount() = comments.size
}