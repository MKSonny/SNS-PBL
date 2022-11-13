package com.example.firebasestoreandauth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp

data class Item(
    val uid: String, val postImgUrl: String, val likes: Number, val time: Timestamp,
    val comments: Map<String, String>
)

enum class ItemNotify {
    ADD, UPDATE, DELETE
}

class MyViewModel : ViewModel() {

    val items = ArrayList<Item>()

    var curUser: String = ""

    fun setUser(curUser: String) {
        this.curUser = curUser
    }

    fun getUser(): String {
        return curUser
    }

    val itemLiveData = MutableLiveData<ArrayList<Item>>()

    var itemNotified: Int = -1
    var itemNotifiedType: ItemNotify = ItemNotify.ADD

    val itemsSize
        get() = items.size

    init {
        //addItem(Item("son", "gs://sns-pbl.appspot.com/상상부기 2.png"))
        //addItem(Item("j", "s"))
    }

    fun addItem(item: Item) {
        itemNotifiedType = ItemNotify.ADD
        itemNotified = itemsSize
        items.add(item)
        itemLiveData.value = items
    }

    fun updateItem(item: Item, pos: Int) {
        itemNotifiedType = ItemNotify.UPDATE
        itemNotified = pos
        items[pos] = item
        itemLiveData.value = items
    }

    fun addItem(pos: Int) {
        itemNotifiedType = ItemNotify.DELETE
        itemNotified = pos
        items.removeAt(pos)
        itemLiveData.value = items
    }
}



