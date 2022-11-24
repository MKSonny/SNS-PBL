package com.example.firebasestoreandauth.fragments.friend

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
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasestoreandauth.R
import com.example.firebasestoreandauth.adapter.SearchResultAdapter
import com.example.firebasestoreandauth.databinding.FragmentFriendSearchBinding
import com.example.firebasestoreandauth.dto.User
import com.example.firebasestoreandauth.utils.extentions.toUser
import com.example.firebasestoreandauth.utils.getReferenceOfMine
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchFriendFragment : Fragment() {
    private var _binding: FragmentFriendSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchResultViewModel by viewModels()

    private var queryResult = mutableListOf<User>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentFriendSearchBinding.inflate(inflater, container, false)
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
        toolbar.title = ""
        setupMenu()

        viewModel.addObserver(viewLifecycleOwner) { adapter.notifyDataSetChanged() }

        binding.searchButton.setOnClickListener {
            val keyword = binding.keyword.text.toString()
            searchFriendWithKeyword(keyword)
        }
        getSomeUser()

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

    private fun getSomeUser() {
        val db = Firebase.firestore
        db.collection("Users")
            .limit(10)
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

        fun addObserver(lifecycleOwner: LifecycleOwner, callback: () -> Unit) {
            _list.observe(lifecycleOwner) {
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
