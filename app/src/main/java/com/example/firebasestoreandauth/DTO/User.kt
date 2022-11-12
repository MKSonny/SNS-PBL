package com.example.firebasestoreandauth.DTO

data class User(
    var UID: String? = null, var NickName: String? = null, var profileImage:String?, var BirthDay: String? = null,
    var friends: List<String>? = listOf(),
    var pendingFriends: List<String>? = listOf()
) {
}