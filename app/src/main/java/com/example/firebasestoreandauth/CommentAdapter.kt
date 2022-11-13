package com.example.firebasestoreandauth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.databinding.CommentItemLayoutBinding
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(private val db: FirebaseFirestore, private val comments: ArrayList<Map<String, String>>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    // var storage = Firebase.storage
    private lateinit var commentMap : Map<String,String>
    private lateinit var string: String

    inner class ViewHolder(private val binding: CommentItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        init {

        }

        fun setContents(pos: Int) {
            binding.commentId.text = comments[pos].keys.toString().replace("[","").replace("]","")
            binding.commentText.text = comments[pos].values.toString().replace("[","").replace("]","")
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