package com.chatapp.users

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatapp.chat.ChatActivity
import com.chatapp.databinding.ActivityUsersBinding
import com.chatapp.model.User
import com.chatapp.utils.FirebaseUtils

class UsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersBinding
    private val allUsers = mutableListOf<User>()
    private val filteredUsers = mutableListOf<User>()
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Chat"

        adapter = UsersAdapter(filteredUsers) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverId", user.uid)
            intent.putExtra("receiverName", user.name)
            startActivity(intent)
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadUsers()
    }

    private fun loadUsers() {
        FirebaseUtils.usersCollection().get()
            .addOnSuccessListener { snapshot ->
                allUsers.clear()
                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java) ?: continue
                    // Don't show current user
                    if (user.uid == FirebaseUtils.currentUserId) continue
                    allUsers.add(user)
                }
                filteredUsers.clear()
                filteredUsers.addAll(allUsers)
                adapter.notifyDataSetChanged()
            }
    }

    private fun filterUsers(query: String) {
        filteredUsers.clear()
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers)
        } else {
            val lower = query.lowercase()
            filteredUsers.addAll(allUsers.filter {
                it.name.lowercase().contains(lower) || it.email.lowercase().contains(lower)
            })
        }
        adapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
