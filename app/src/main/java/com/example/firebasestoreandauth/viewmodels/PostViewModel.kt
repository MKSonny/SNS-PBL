package com.example.firebasestoreandauth.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firebasestoreandauth.dto.Item


enum class ItemNotify {
    ADD, UPDATE, DELETE, RESET
}

class PostViewModel : ViewModel() {
    // 현재 앱을 사용하는 사용자 이름
    private var meInfo: String = "Son"
    private lateinit var commentPostInfo: String

    fun getMeInfo(): String {
        return meInfo
    }

    fun notifyClickedPostInfo(): String {
        return commentPostInfo
    }

    fun ClickedPostInfo(postDocInfo: String) {
        commentPostInfo = postDocInfo
    }

    private var curPos: Int = 0

    val items = ArrayList<Item>()

    var curUser: String = ""

    fun setUser(curUser: String) {
        this.curUser = curUser
    }

    fun getUser(): String {
        return curUser
    }

    fun allItems(): ArrayList<Item> {
        return items
    }

    fun addComments(pos: Int, map: Map<String, String>) {
        items[pos].comments.add(map)
    }

    fun setComments(new_comments: ArrayList<Map<String, String>>) {
        items[getPos()].comments = new_comments
    }

    val itemLiveData = MutableLiveData<ArrayList<Item>>()

    var itemNotified: Int = -1
    var itemNotifiedType: ItemNotify = ItemNotify.ADD

    fun setPos(pos: Int) {
        curPos = pos
    }

    fun getPos(): Int {
        return curPos
    }

    fun getComment(pos: Int): ArrayList<Map<String, String>> {
        println("#######" + items.size)
        return items[pos].comments
    }

    val itemsSize
        get() = items.size

    init {
        //addItem(Item("son", "gs://sns-pbl.appspot.com/상상부기 2.png"))
        //addItem(Item("j", "s"))
    }

    fun clearAll() {
        itemNotifiedType = ItemNotify.ADD
        items.clear()
        itemLiveData.value = items

    }

    fun addToFirst(item: Item) {
        itemNotifiedType = ItemNotify.ADD
        itemNotified = itemsSize
        items.add(0,item)
        //items.add(item)
        itemLiveData.value = items
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

    fun deleteItem(pos: Int) {
        itemNotifiedType = ItemNotify.DELETE
        itemNotified = pos
        items.removeAt(pos)
        itemLiveData.value = items
    }
}

