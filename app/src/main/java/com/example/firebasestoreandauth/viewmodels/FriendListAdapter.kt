package com.example.firebasestoreandauth.viewmodels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.FriendListItemBinding
import com.example.firebasestoreandauth.wrapper.deleteFriend
import com.example.firebasestoreandauth.wrapper.getUserDocumentWith
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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
            nickname.text = user.nickname
            nickname.setOnLongClickListener {
                val menu = PopupMenu(it.context, it)
                menu.inflate(R.menu.popup_friend_item)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.delete_friend -> {
                            user.uid?.let { it1 ->
                                if (it1 != User.Companion.INVALID_USER) getUserDocumentWith(
                                    it1
                                )?.deleteFriend()
                            }
                            println("You selected delete")
                        }
                    }
                    true
                }
                menu.show()
                println("LongClicked")
                return@setOnLongClickListener true
            }
            if (user.profileImage == null || user.profileImage == User.INVALID_USER
                || (user.profileImage ?: "").isEmpty() ||user.profileImage=="null"
            ) return
            image.clipToOutline = true
            val stRef = Firebase.storage
            val pathRef = stRef.getReferenceFromUrl(user.profileImage!!)
            pathRef.getBytes(3 * 1024 * 1024).addOnCompleteListener{
                if(it.isSuccessful)
                    Glide.with(image.rootView.context).asBitmap().load(it.result).into(image)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FriendListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)
        val binding = holder

    }


    override fun getItemCount(): Int {
        return viewModel.friend.getSize()
    }
}