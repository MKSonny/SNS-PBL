package com.example.firebasestoreandauth.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.FriendsLayoutBinding
import com.example.firebasestoreandauth.viewmodels.FriendListAdapter
import com.example.firebasestoreandauth.viewmodels.FriendViewModel
import com.example.firebasestoreandauth.viewmodels.RequestReceivedAdapter
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.example.firebasestoreandauth.wrapper.getUserCollection
import com.example.firebasestoreandauth.wrapper.toUser
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * 친구와 관련된 정보를 표시하는 프래그먼트
 * 표시하는 정보 :
 * 1. 현재 친구 목록
 * 2. '나'에게 친구 요청을 보낸 사용자 목록
 * 이동 가능:
 * 네비게이션을 이용해 다음으로 이동 가능
 * 1. 하단을 통해 프로필과 게시글
 * 2. 툴바의 +버튼을 이용해 친구 찾기
 */
class FriendsFragment : Fragment(R.layout.friends_layout) {
    var snapshotListener: ListenerRegistration? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FriendsLayoutBinding.bind(view)
        val friendModel: FriendViewModel by viewModels()
        val listAdapter = FriendListAdapter(friendModel)
        val requestAdapter = RequestReceivedAdapter(friendModel)
        val toolbar = binding.friendToolbar

         (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setupMenu()
        toolbar.title = ""//추후 수정해야 할 수 있음 https://dreamaz.tistory.com/102


        friendModel.apply {
            friend.observe(viewLifecycleOwner) {
                listAdapter.notifyDataSetChanged()
            }
            requestReceived.observe(viewLifecycleOwner) {
                requestAdapter.notifyDataSetChanged()
            }
        }

        binding.recyclerFriendList.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        binding.recyclerReceivedList.apply {
            adapter = requestAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        Firebase.firestore.clearPersistence()
        snapshotListener = getReferenceOfMine()?.addSnapshotListener (MetadataChanges.INCLUDE){ snapshot, e ->
            val TAG = "SnapshotListener"
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                //먼저 나에 대한 문서 참조를 가져옴
                val user = snapshot.toUser()
                if (user.uid == User.INVALID_USER) return@addSnapshotListener
                Log.d(TAG, "Current data: ${user}")
                updateView(user, friendModel,listAdapter, requestAdapter)

            } else {
                Log.d(TAG, "Current data: null")
            }

        }
    }

    private fun updateView(user: User, friendModel: FriendViewModel, listAdapter:FriendListAdapter, requestReceivedAdapter: RequestReceivedAdapter) {
        val col = getUserCollection()
        if (user.friends!!.isNotEmpty()) {
            col.whereIn("uid", user.friends!!).get().addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    val list = snapshot.result.documents.map {
                        it.toUser()
                    }.toList()
                    friendModel.friend.setList(list)
                }
            }
        }
        else{
            friendModel.friend.setList(emptyList())
        }
        if (user.requestReceived!!.isNotEmpty()) {
            col.whereIn("uid", user.requestReceived!!).get().addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    val list = snapshot.result.documents.map {
                        it.toUser()
                    }.toList()
                    friendModel.requestReceived.setList(list)
                }
            }
        }else{
            friendModel.requestReceived.setList(emptyList())
        }
        listAdapter.notifyDataSetChanged()
        requestReceivedAdapter.notifyDataSetChanged()
    }


    private fun setupMenu() {
        val host = (requireActivity() as MenuHost)
        host.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.friend_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.add_friend -> {
                            findNavController()?.navigate(R.id.action_friendsFragment_to_searchFriendActivity)
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }

            }, viewLifecycleOwner, Lifecycle.State.RESUMED
        )
    }
}