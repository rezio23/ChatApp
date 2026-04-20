package com.chatapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatapp.databinding.ItemChatPreviewBinding
import com.chatapp.model.ChatPreview
import com.chatapp.utils.TimeUtils

class ChatListAdapter(
    private val chats: List<ChatPreview>,
    private val onClick: (ChatPreview) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemChatPreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatPreviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.binding.apply {
            tvName.text = chat.user.name
            tvLastMessage.text = if (chat.lastMessage.isEmpty()) "Tap to chat" else chat.lastMessage
            tvTime.text = if (chat.lastMessageTime > 0) TimeUtils.formatChatListTime(chat.lastMessageTime) else ""

            if (chat.user.profileImageUrl.isNotEmpty()) {
                Glide.with(root.context)
                    .load(chat.user.profileImageUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }

            root.setOnClickListener { onClick(chat) }
        }
    }

    override fun getItemCount() = chats.size
}
