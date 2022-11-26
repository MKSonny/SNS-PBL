package com.example.firebasestoreandauth.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.ItemFriendListBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.extentions.deleteFriend
import com.example.firebasestoreandauth.utils.getUserDocumentWith
import com.example.firebasestoreandauth.viewmodels.FriendViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FriendListAdapter() :
    RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {
    private val differ = AsyncListDiffer(this, differCallback)

    inner class ViewHolder(
        val binding: ItemFriendListBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setContent(idx: Int) {
            val user = differ.currentList[position]
            binding.apply {
                friendNickname.text = user.nickname
                friendNickname.setOnLongClickListener {
                    val menu = PopupMenu(it.context, it)
                    menu.inflate(R.menu.popup_friend_item)
                    menu.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.delete_friend -> {
                                user.uid?.let { it1 ->
                                    if (it1 != User.INVALID_USER) getUserDocumentWith(
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
                    || (user.profileImage ?: "").isEmpty() || user.profileImage == "null"
                ) return
                friendProfileImage.clipToOutline = true
                val stRef = Firebase.storage
                try {
                    val pathRef = stRef.getReferenceFromUrl(user.profileImage!!)
                    pathRef.getBytes(3 * 1024 * 1024).addOnCompleteListener {
                        if (it.isSuccessful)
                            Glide.with(friendProfileImage.rootView.context).asBitmap()
                                .load(it.result)
                                .into(friendProfileImage)
                    }
                } catch (_: Exception) {
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFriendListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<User>) {
        val x = mutableListOf<User>().apply {
            addAll(list)
        }
        differ.submitList(x.toList())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)
    }

    companion object {
        val differCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.uid.toString() == newItem.uid.toString()
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }

    }

}