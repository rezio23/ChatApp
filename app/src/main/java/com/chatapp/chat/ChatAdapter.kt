package com.chatapp.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chatapp.databinding.ItemMessageReceivedBinding
import com.chatapp.databinding.ItemMessageSentBinding
import com.chatapp.model.Message
import com.chatapp.utils.TimeUtils

class ChatAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    inner class SentViewHolder(val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT
        else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            SentViewHolder(
                ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            ReceivedViewHolder(
                ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentViewHolder -> {
                holder.binding.tvMessage.text = message.message
                holder.binding.tvTime.text = TimeUtils.formatMessageTime(message.timestamp)
                // Show read receipt
                holder.binding.ivReadReceipt.setImageResource(
                    if (message.isRead) com.chatapp.R.drawable.ic_read
                    else com.chatapp.R.drawable.ic_sent
                )
            }
            is ReceivedViewHolder -> {
                holder.binding.tvMessage.text = message.message
                holder.binding.tvTime.text = TimeUtils.formatMessageTime(message.timestamp)
            }
        }
    }

    override fun getItemCount() = messages.size
}
