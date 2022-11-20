package com.example.firebasestoreandauth.viewmodels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.databinding.FriendListItemBinding

class FriendListAdapter(private val viewModel: FriendViewModel) :
    RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {
    inner class ViewHolder(
        binding: FriendListItemBinding,
        private val viewModel: FriendViewModel
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val nickname = binding.friendNickname
        private val image = binding.friendProfileImage
        fun setContent(idx: Int) {
            val user = viewModel.friend.getItem(idx)
            nickname.text = user
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FriendListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)
    }

    override fun getItemCount(): Int {
        return viewModel.friend.getSize()
    }
}