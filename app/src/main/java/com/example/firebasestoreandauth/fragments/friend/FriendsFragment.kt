package com.example.firebasestoreandauth.fragments.friend

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.adapter.FriendListAdapter
import com.example.firebasestoreandauth.adapter.RequestReceivedAdapter
import com.example.firebasestoreandauth.databinding.FragmentFriendsMainBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.extentions.toUser
import com.example.firebasestoreandauth.utils.getReferenceOfMine
import com.example.firebasestoreandauth.utils.getUserCollection
import com.example.firebasestoreandauth.viewmodels.FriendViewModel
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
class FriendsFragment : Fragment(R.layout.fragment_friends_main) {
    var snapshotListener: ListenerRegistration? = null
    private val friendModel: FriendViewModel by activityViewModels()
    private var _binding: FragmentFriendsMainBinding? = null
    private var listAdapter: FriendListAdapter? = null
    private var requestAdapter: RequestReceivedAdapter? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = FriendListAdapter()
        requestAdapter = RequestReceivedAdapter()
        snapshotListener =
            getReferenceOfMine()?.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
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
                    updateView(user, friendModel, listAdapter!!, requestAdapter!!)
                } else {
                    Log.d(TAG, "Current data: null")
                }

            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsMainBinding.inflate(inflater, container, false)

        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        listAdapter = null
        requestAdapter = null
        snapshotListener?.remove()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = binding.friendToolbar

        binding.recyclerFriendList.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.recyclerReceivedList.apply {
            adapter = requestAdapter
            layoutManager = LinearLayoutManager(context)
        }

        friendModel.apply {
            friend.observe(viewLifecycleOwner) {
                (binding.recyclerFriendList.adapter as FriendListAdapter).submitList(it)

            }
            requestReceived.observe(viewLifecycleOwner) {
                (binding.recyclerReceivedList.adapter as RequestReceivedAdapter).submitList(it)
            }
        }
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        setupMenu()
        toolbar.title = ""//추후 수정해야 할 수 있음 https://dreamaz.tistory.com/102
    }

    private fun updateView(
        user: User,
        friendModel: FriendViewModel,
        listAdapter: FriendListAdapter,
        requestReceivedAdapter: RequestReceivedAdapter
    ) {
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
        } else {
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
        } else {
            friendModel.requestReceived.setList(emptyList())
        }
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