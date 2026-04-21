package com.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatapp.auth.LoginActivity
import com.chatapp.chat.ChatActivity
import com.chatapp.databinding.ActivityMainBinding
import com.chatapp.model.ChatPreview
import com.chatapp.model.User
import com.chatapp.users.UsersActivity
import com.chatapp.utils.FirebaseUtils
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val chatPreviews = mutableListOf<ChatPreview>()
    private lateinit var adapter: ChatListAdapter
    private var chatsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "ChatApp"

        adapter = ChatListAdapter(chatPreviews) { chatPreview ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverId", chatPreview.user.uid)
            intent.putExtra("receiverName", chatPreview.user.name)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
        }

        loadChats()
    }

    private fun loadChats() {
        val currentUid = FirebaseUtils.currentUserId
        if (currentUid.isEmpty()) return

        Log.d("MainActivity", "Loading chats for UID: $currentUid")

        chatsListener = FirebaseUtils.chatsCollection()
            .document(currentUid)
            .collection("userChats")
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MainActivity", "Error loading chats: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("MainActivity", "No chats found in Firestore")
                    binding.tvEmptyState.visibility = View.VISIBLE
                    chatPreviews.clear()
                    adapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                binding.tvEmptyState.visibility = View.GONE
                val tempPreviews = mutableListOf<ChatPreview>()
                var loadedCount = 0
                val docs = snapshot.documents

                for (doc in docs) {
                    val otherUserId = doc.getString("userId") ?: continue
                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val lastMessageTime = doc.getLong("lastMessageTime") ?: 0L

                    FirebaseUtils.usersCollection().document(otherUserId).get()
                        .addOnSuccessListener { userDoc ->
                            val user = userDoc.toObject(User::class.java)
                            if (user != null) {
                                tempPreviews.add(
                                    ChatPreview(
                                        user = user,
                                        lastMessage = lastMessage,
                                        lastMessageTime = lastMessageTime
                                    )
                                )
                            }
                            loadedCount++
                            if (loadedCount == docs.size) {
                                chatPreviews.clear()
                                chatPreviews.addAll(tempPreviews.sortedByDescending { it.lastMessageTime })
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainActivity", "Error fetching user details for $otherUserId", e)
                            loadedCount++
                            if (loadedCount == docs.size) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseUtils.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }
            R.id.menu_new_chat -> {
                startActivity(Intent(this, UsersActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatsListener?.remove()
    }
}
