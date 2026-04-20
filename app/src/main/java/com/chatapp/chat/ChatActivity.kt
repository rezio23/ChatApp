package com.chatapp.chat

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatapp.databinding.ActivityChatBinding
import com.chatapp.model.Message
import com.chatapp.utils.FirebaseUtils
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private var messagesListener: ListenerRegistration? = null

    private lateinit var receiverId: String
    private lateinit var receiverName: String
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiverId = intent.getStringExtra("receiverId") ?: run { finish(); return }
        receiverName = intent.getStringExtra("receiverName") ?: "Chat"
        chatRoomId = FirebaseUtils.getChatRoomId(FirebaseUtils.currentUserId, receiverId)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = receiverName

        adapter = ChatAdapter(messages, FirebaseUtils.currentUserId)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }

        listenForMessages()
    }

    private fun listenForMessages() {
        messagesListener = FirebaseUtils.messagesCollection()
            .document(chatRoomId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                messages.clear()
                for (doc in snapshot.documents) {
                    val message = doc.toObject(Message::class.java)
                    if (message != null) messages.add(message)
                }
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) {
                    binding.recyclerView.scrollToPosition(messages.size - 1)
                }
                markMessagesAsRead()
            }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        binding.etMessage.setText("")

        val currentUserId = FirebaseUtils.currentUserId
        val messageId = FirebaseUtils.messagesCollection().document().id
        val timestamp = System.currentTimeMillis()

        val message = Message(
            messageId = messageId,
            senderId = currentUserId,
            receiverId = receiverId,
            message = text,
            timestamp = timestamp,
            isRead = false,
            type = "text"
        )

        // Save message in the shared chat room
        FirebaseUtils.messagesCollection()
            .document(chatRoomId)
            .collection("chats")
            .document(messageId)
            .set(message)
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }

        // Update chat preview for sender
        updateChatPreview(currentUserId, receiverId, text, timestamp)
        // Update chat preview for receiver
        updateChatPreview(receiverId, currentUserId, text, timestamp)
    }

    private fun updateChatPreview(ownerId: String, otherUserId: String, lastMessage: String, timestamp: Long) {
        val chatData = hashMapOf(
            "userId" to otherUserId,
            "lastMessage" to lastMessage,
            "lastMessageTime" to timestamp
        )
        FirebaseUtils.chatsCollection()
            .document(ownerId)
            .collection("userChats")
            .document(otherUserId)
            .set(chatData)
    }

    private fun markMessagesAsRead() {
        val currentUserId = FirebaseUtils.currentUserId
        FirebaseUtils.messagesCollection()
            .document(chatRoomId)
            .collection("chats")
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = FirebaseUtils.firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.remove()
    }
}
