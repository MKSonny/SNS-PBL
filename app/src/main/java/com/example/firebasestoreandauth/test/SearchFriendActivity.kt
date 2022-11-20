package com.example.firebasestoreandauth.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasestoreandauth.DTO.User
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.databinding.FragmentSearchFriendBinding
import com.example.firebasestoreandauth.databinding.FriendQueryItemLayoutBinding
import com.example.firebasestoreandauth.wrapper.getReferenceOfMine
import com.example.firebasestoreandauth.wrapper.getUserDocumentWith
import com.example.firebasestoreandauth.wrapper.sendRequestToFriend
import com.example.firebasestoreandauth.wrapper.toUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchFriendActivity : Fragment() {
    private var _binding: FragmentSearchFriendBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchResultViewModel by viewModels()

    private var queryResult = mutableListOf<User>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSearchFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onCreate(savedInstanceState)
        val adapter = SearchResultAdapter(viewModel)
        val toolbar = binding.toolbar

        //툴바 세팅
        binding.apply {
            queryHolder.adapter = adapter
            queryHolder.layoutManager = LinearLayoutManager(requireActivity())
            queryHolder.setHasFixedSize(true)
        }

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.title = "친구 찾기"
        setupMenu()

        viewModel.addObserver(viewLifecycleOwner) { adapter.notifyDataSetChanged() }

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

    private fun setupMenu() {
        (requireActivity() as MenuHost)
            .addMenuProvider(
                object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                        menuInflater.inflate(R.menu.find_friend, menu)
                    }

                    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                        return when (menuItem.itemId) {
                            R.id.search -> {
                                val keyword = binding.keyword.text.toString()
                                searchFriendWithKeyword(keyword)
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

    private fun searchFriendWithKeyword(keyword: String) {
        val db = Firebase.firestore
        db.collection("Users")
            .whereEqualTo("nickname", keyword)
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

    class SearchResultViewModel() : ViewModel() {
        private var _list: MutableLiveData<List<User>> = MutableLiveData(emptyList())

        fun addObserver(lifecycleOwner: LifecycleOwner, callback: ()->Unit){
            _list.observe(lifecycleOwner){
               callback()
            }
        }

        fun setQueryResult(list: List<User>) {
            _list.value = list.toList()
        }

        fun getItem(idx: Int): User {
            return (
                    if (idx > _list.value!!.size)
                        User(uid = "-1", profileImage = "-1")
                    else
                        _list.value!![idx])
        }

        fun getSize(): Int = _list.value!!.size
    }
}

class SearchResultViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchFriendActivity.SearchResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchFriendActivity.SearchResultViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SearchResultAdapter(val viewModel: SearchFriendActivity.SearchResultViewModel) :
    RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    class ViewHolder(
        binding: FriendQueryItemLayoutBinding,
        val viewModel: SearchFriendActivity.SearchResultViewModel
    ) :
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
                        reference.sendRequestToFriend()
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
