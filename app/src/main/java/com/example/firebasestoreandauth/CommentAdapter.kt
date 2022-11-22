package com.example.firebasestoreandauth

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.databinding.CommentItemLayoutBinding
import com.example.firebasestoreandauth.databinding.CommentLayoutBinding
import com.example.firebasestoreandauth.databinding.ItemLayoutBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Objects


class CommentAdapter(private val db: FirebaseFirestore, private val comments: ArrayList<Map<String, String>>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    val storage = Firebase.storage
    var moreThan = false

    inner class ViewHolder(private val binding: CommentItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setContents(pos: Int) {
                db.collection("SonUsers")
                    .document(comments[pos].keys.toString().replace("[", "").replace("]", ""))
                    .get().addOnSuccessListener {

                        binding.commentId.setText(it["nickName"].toString())
                        val profile_img = it["profileImage"].toString()
                        val profileImageRef = storage.getReferenceFromUrl(profile_img)
                        profileImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                            binding.profileImg.setImageBitmap(bmp)
                        }.addOnFailureListener {}
                    }
                //binding.commentId.text = comments[pos].keys.toString().replace("[","").replace("]","")
                binding.commentText.text =
                    comments[pos].values.toString().replace("[", "").replace("]", "")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CommentItemLayoutBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContents(position)
    }

    override fun getItemCount() = comments.size
}