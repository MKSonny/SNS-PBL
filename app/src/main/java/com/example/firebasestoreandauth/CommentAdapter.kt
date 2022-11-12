package com.example.firebasestoreandauth

import android.graphics.BitmapFactory
import android.view.LayoutInflater
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

class CommentAdapter(private val db: FirebaseFirestore, private val navigate: NavController, private val viewModel: MyViewModel) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    // var storage = Firebase.storage

    inner class ViewHolder(private val binding: CommentItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setContents(pos: Int) {
            val item = viewModel.items[pos]
            val comments = item.comments

            for (it in comments) {
                binding.commentId.text = it.key
                binding.commentText.text = it.value
            }
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

    override fun getItemCount() = viewModel.itemsSize
}