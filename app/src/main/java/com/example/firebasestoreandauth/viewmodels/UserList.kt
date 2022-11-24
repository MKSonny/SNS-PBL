package com.example.firebasestoreandauth.viewmodels

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.firebasestoreandauth.dto.User

class UserList {
    private val list = MutableLiveData<List<User>>()
    fun observe(owner: LifecycleOwner, observer: Observer<List<User>>) {
        list.observe(owner, observer)
    }

    init {
        list.value = listOf()
    }

    fun setList(list: List<User>) {
        this.list.value = list
    }

    fun getItem(idx: Int): User {
        return (
                if (idx > list.value!!.size)
                    User(uid = User.INVALID_USER, "", "")
                else
                    list.value!![idx])
    }

    fun getSize(): Int = list.value!!.size
}
