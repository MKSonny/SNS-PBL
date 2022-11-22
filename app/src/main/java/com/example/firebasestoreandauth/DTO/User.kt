package com.example.firebasestoreandauth.DTO

data class User(
    var uid: String? = null,
    var nickname: String? = null,
    var profileImage:String?, var BirthDay: String? = null,
    var friends: List<String>? = listOf(),
    var requestSent: List<String>? = listOf(),
    var requestReceived:List<String>? = listOf()
) {
    companion object{
        const val INVALID_USER = "-1"
    }
}