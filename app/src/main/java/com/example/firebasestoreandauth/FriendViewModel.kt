package com.example.firebasestoreandauth

import androidx.lifecycle.ViewModel

class FriendViewModel() : ViewModel() {
    public val friend = UserList()
    public val requestReceived = UserList()
}