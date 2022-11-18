package com.example.firebasestoreandauth.test

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.databinding.ActivitySearchFriendBinding
import com.example.firebasestoreandauth.databinding.FriendQueryItemLayoutBinding
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.example.firebasestoreandauth.wrapper.getUserDocumentWith
import com.example.firebasestoreandauth.wrapper.sendRequestToFriend
import com.example.firebasestoreandauth.wrapper.toUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchFriendBinding
    private var queryResult = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.searchButton.setOnClickListener {
            val keyword = binding.keyword.text.toString() //keyword : Email, NickName, RealName
            searchFriendWithKeyword(keyword)
        }
        binding.requestToFriend.setOnClickListener {
            sendRequest()
        }

        val viewModel: SearchResultViewModel by viewModels()
        val adapter = SearchResultAdapter(viewModel)

        binding.queryHolder.adapter = adapter
        binding.queryHolder.layoutManager = LinearLayoutManager(this)
        binding.queryHolder.setHasFixedSize(true)

        getReferenceOfMine()?.addSnapshotListener { snapshot, e ->
            val TAG = "SnapshotListener"
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.toUser()}")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun sendRequest() {
        val uid = Firebase.auth.currentUser?.uid
        val idx = binding.editIndexButton
            .text
            .toString().toInt()
        if (Firebase.auth.currentUser != null) {
            //For test purpose only!
            if (idx > queryResult.size - 1)
                return
            val uid = queryResult[idx].uid
            if (uid != null) {
                val reference = getUserDocumentWith(uid)
                reference?.get()?.addOnSuccessListener { _ ->
                    reference.sendRequestToFriend(uid)
                }
            }
        }
    }


    private fun searchFriendWithKeyword(keyword: String) {
        val db = Firebase.firestore
        db.collection("Users")
            .whereEqualTo("nickName", keyword)
            .get()
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val documentRef = it.result.documents
                    queryResult.clear()
                    documentRef.map {
                        queryResult.add(it.toUser())
                        val viewModel: SearchResultViewModel by viewModels()
                        viewModel.setQueryResult(queryResult)
                    }
                } else {
                    println(it.exception)
                }
            }
    }

}

class SearchResultViewModel() : ViewModel() {
    private var _list: List<User> = listOf()

    fun setQueryResult(list: List<User>) {
        _list = list.toList()
    }

    fun getItem(idx: Int): User {
        return (
                if (idx > _list.size)
                    User(uid = "-1", profileImage = "-1")
                else
                    _list[idx])
    }

    fun getSize(): Int = _list.size

}

class SearchResultViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchResultViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SearchResultAdapter(val viewModel: SearchResultViewModel) :
    RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    class ViewHolder(binding: FriendQueryItemLayoutBinding, val viewModel: SearchResultViewModel) :
        RecyclerView.ViewHolder(binding.root) {
        private val nickname = binding.queryFriendNickname
        val image = binding.queryFriendProfileImage
        private val addButton = binding.queryFriendAdd
        fun setContent(idx: Int) {
            val record = viewModel.getItem(idx)
            if (record.uid != "-1")
                nickname.text = record.nickname
            //TODO: GetImage From Storage
            addButton.setOnClickListener {
                if (Firebase.auth.currentUser != null && record.uid != null) {
                    val uid = record.uid
                    val reference = getUserDocumentWith(uid!!)
                    reference?.get()?.addOnSuccessListener { _ ->
                        reference.sendRequestToFriend(uid)
                    }
                }
                Log.d("SearchResultAdapter", "You clicked ${record.nickname}")
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = FriendQueryItemLayoutBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setContent(position)
    }

    override fun getItemCount(): Int {
        return viewModel.getSize()
    }
}
