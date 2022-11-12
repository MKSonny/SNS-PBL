package com.example.firebasestoreandauth

import androidx.lifecycle.ViewModel

data class Item(val name: String, val name2: String)

class MyViewModel : ViewModel() {

    val items = ArrayList<Item>()

    init {
        addItem(Item("j", "s"))
        addItem(Item("j", "s"))
    }

    fun addItem(item: Item) {
        items.add(item)
    }

    fun updateItem(item: Item, pos: Int) {
        items[pos] = item
    }

    fun addItem(pos: Int) {
        items.removeAt(pos)
    }
}