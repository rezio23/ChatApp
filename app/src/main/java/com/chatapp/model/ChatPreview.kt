package com.chatapp.model

data class ChatPreview(
    val user: User = User(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0
)
