package com.example.firebasestoreandauth.viewmodels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.FriendRequestReceivedItemLayoutBinding
import com.example.firebasestoreandauth.wrapper.acceptFriendRequest
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.example.firebasestoreandauth.wrapper.getUserDocumentWith
import com.example.firebasestoreandauth.wrapper.rejectFriendRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class RequestReceivedAdapter(private val viewModel: FriendViewModel) :
    RecyclerView.Adapter<RequestReceivedAdapter.ViewHolder>() {
    inner class ViewHolder(
        private val binding: FriendRequestReceivedItemLayoutBinding,
        private val viewModel: FriendViewModel
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val nickname = binding.requestReceivedNickname
        private val image = binding.requestReceivedProfileImage
        private val acceptButton = binding.acceptRequestButton
        private val rejectButton = binding.rejectRequestButton

        fun setContent(idx: Int) {
            val user = viewModel.requestReceived.getItem(idx)
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
            ) return
            image.clipToOutline = true
            val stRef = Firebase.storage
            val pathRef = stRef.getReferenceFromUrl(user.profileImage!!)
            pathRef.getBytes(3 * 1024 * 1024).addOnCompleteListener {
                if (it.isSuccessful)
                    Glide.with(image.rootView.context).asBitmap().load(it.result).into(image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FriendRequestReceivedItemLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)
    }

    override fun getItemCount(): Int {
        return viewModel.requestReceived.getSize()
    }
}