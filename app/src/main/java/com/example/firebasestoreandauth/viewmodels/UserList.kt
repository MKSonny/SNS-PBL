package com.example.firebasestoreandauth.viewmodels

import androidx.lifecycle.*

class UserList {
    private val list = MutableLiveData<List<String>>()
    fun observe(owner: LifecycleOwner, observer: Observer<List<String>>) {
        list.observe(owner, observer)
    }

    init {
        list.value = listOf()
    }

    fun setList(list: List<String>) {
        this.list.value = list
    }

    fun getItem(idx: Int): String {
        return (
                if (idx > list.value!!.size)
                    ""
                else
                    list.value!![idx])
    }

    fun getSize(): Int = list.value!!.size
}
