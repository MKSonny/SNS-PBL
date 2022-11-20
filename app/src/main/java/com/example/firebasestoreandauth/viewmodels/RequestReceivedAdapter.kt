package com.example.firebasestoreandauth.viewmodels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.databinding.RequestReceivedItemLayoutBinding
import com.example.firebasestoreandauth.wrapper.acceptFriendRequest
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.google.android.material.snackbar.Snackbar

class RequestReceivedAdapter(private val viewModel: FriendViewModel) :
    RecyclerView.Adapter<RequestReceivedAdapter.ViewHolder>() {
    inner class ViewHolder(
        private val binding: RequestReceivedItemLayoutBinding,
        private val viewModel: FriendViewModel
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val nickname = binding.requestReceivedNickname
        private val image = binding.requestReceivedProfileImage
        private val acceptButton = binding.acceptRequestButton
        fun setContent(idx: Int) {
            val uid = viewModel.requestReceived.getItem(idx)
            nickname.text = uid
            acceptButton.setOnClickListener {
                getReferenceOfMine()?.acceptFriendRequest(uid)
                Snackbar.make(binding.root, "$uid", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RequestReceivedItemLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)
    }

    override fun getItemCount(): Int {
        return viewModel.requestReceived.getSize()
    }
}