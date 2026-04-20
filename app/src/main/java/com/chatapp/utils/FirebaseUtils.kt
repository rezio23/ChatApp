package com.chatapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtils {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun usersCollection() = firestore.collection("users")
    fun messagesCollection() = firestore.collection("messages")
    fun chatsCollection() = firestore.collection("chats")

    // Chat room ID: always sorted so both users get same ID
    fun getChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}"
        else "${userId2}_${userId1}"
    }
}
