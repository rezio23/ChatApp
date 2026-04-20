package com.chatapp.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val status: String = "Hey there! I am using ChatApp.",
    val lastSeen: Long = 0L
)
