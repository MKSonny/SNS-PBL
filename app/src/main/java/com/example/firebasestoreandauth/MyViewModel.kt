package com.example.firebasestoreandauth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp

data class Item(
    val postId: String,
    val postImgUrl: String,
    //val likes: Number,
    //val time: Timestamp,
    val whoPosted: String,
    val comments: ArrayList<Map<String, String>>
)

enum class ItemNotify {
    ADD, UPDATE, DELETE
}

class MyViewModel : ViewModel() {
    // 현재 앱을 사용하는 사용자 이름
    private var meInfo: String = "Son"

    fun getMeInfo() : String {
        return meInfo
    }

    private var curPos: Int = 0
    private lateinit var postId: String

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

    fun setPostId(postId: String) {
        this.postId = postId
    }

    fun getPostId() : String {
        return postId
    }


    fun setPos(pos: Int) {
        curPos = pos
    }

    fun getPos() : Int{
        return curPos
    }

    fun getComment(pos: Int) : ArrayList<Map<String, String>> {
        println("#######"+items.size)
        return items[pos].comments
    }

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