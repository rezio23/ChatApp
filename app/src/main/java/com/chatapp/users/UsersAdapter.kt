package com.chatapp.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatapp.R
import com.chatapp.databinding.ItemUserBinding
import com.chatapp.model.User

class UsersAdapter(
    private val users: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.apply {
            tvName.text = user.name
            tvEmail.text = user.email

            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(root.context)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }

            root.setOnClickListener { onClick(user) }
        }
    }

    override fun getItemCount() = users.size
}
