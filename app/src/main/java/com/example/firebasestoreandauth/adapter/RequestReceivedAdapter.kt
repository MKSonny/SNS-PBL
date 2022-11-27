package com.example.firebasestoreandauth.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasestoreandauth.databinding.ItemFriendRequestReceivedBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.extentions.acceptFriendRequest
import com.example.firebasestoreandauth.utils.extentions.rejectFriendRequest
import com.example.firebasestoreandauth.utils.getReferenceOfMine
import com.example.firebasestoreandauth.utils.getUserDocumentWith
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class RequestReceivedAdapter() :
    RecyclerView.Adapter<RequestReceivedAdapter.ViewHolder>() {
    inner class ViewHolder(
        private val binding: ItemFriendRequestReceivedBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val nickname = binding.requestReceivedNickname
        private val image = binding.requestReceivedProfileImage
        private val acceptButton = binding.acceptRequestButton
        private val rejectButton = binding.rejectRequestButton

        fun setContent(idx: Int) {
            val user = differ.currentList[idx]
            nickname.text = user.nickname
            acceptButton.setOnClickListener {
                if ((user.uid ?: "").isNotEmpty()) {
                    getReferenceOfMine()?.acceptFriendRequest(user.uid!!)
                }
            }
            rejectButton.setOnClickListener {
                if ((user.uid ?: "").isNotEmpty()) {
                    user.uid?.let { it1 -> if (it1 != User.INVALID_USER) getUserDocumentWith(it1)?.rejectFriendRequest() }
                }
            }
            if (user.profileImage == null || user.profileImage == User.INVALID_USER
                || (user.profileImage ?: "").isEmpty()
            ) {
                try {
                    image.setImageResource(android.R.color.transparent)
                } catch (_: Exception) {
                }
                return
            }
            image.clipToOutline = true
            val stRef = Firebase.storage
            try {
                val pathRef = stRef.getReferenceFromUrl(user.profileImage!!)
                pathRef.getBytes(3 * 1024 * 1024).addOnCompleteListener {
                    if (it.isSuccessful)
                        Glide.with(image.rootView.context).asBitmap().load(it.result).into(image)
                }
            } catch (e: Exception) {
                Log.w("RequestReceivedItem", "${e.message}")
                image.setImageResource(android.R.color.transparent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFriendRequestReceivedBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val differCallback = object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid.toString() == newItem.uid.toString()
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<User>) {
        differ.submitList(list)
    }
}