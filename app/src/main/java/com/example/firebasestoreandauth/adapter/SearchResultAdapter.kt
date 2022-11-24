package com.example.firebasestoreandauth.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasestoreandauth.databinding.ItemFriendQueryBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.fragments.friend.SearchFriendFragment
import com.example.firebasestoreandauth.utils.extentions.sendRequestToFriend
import com.example.firebasestoreandauth.utils.getUserDocumentWith
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SearchResultAdapter(val viewModel: SearchFriendFragment.SearchResultViewModel) :
    RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    class ViewHolder(
        binding: ItemFriendQueryBinding,
        val viewModel: SearchFriendFragment.SearchResultViewModel
    ) :
        RecyclerView.ViewHolder(binding.root) {
        private val nickname = binding.queryFriendNickname
        private val image = binding.queryFriendProfileImage
        private val addButton = binding.queryFriendAdd
        fun setContent(idx: Int) {
            val user = viewModel.getItem(idx)
            if (user.uid != "-1")
                nickname.text = user.nickname
            //TODO: GetImage From Storage
            addButton.setOnClickListener {
                if (Firebase.auth.currentUser != null && user.uid != null) {
                    val uid = user.uid
                    val reference = getUserDocumentWith(uid!!)
                    reference?.get()?.addOnSuccessListener { _ ->
                        reference.sendRequestToFriend()
                    }
                }
                Log.d("SearchResultAdapter", "You clicked ${user.nickname}")
            }

            if (user.profileImage == null || user.profileImage == User.INVALID_USER
                || (user.profileImage ?: "").isEmpty() || user.profileImage == "null"
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
        val binding = ItemFriendQueryBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)
    }

    override fun getItemCount(): Int {
        return viewModel.getSize()
    }
}