package com.chatapp.model

data class Transaction(
    val id: String = "",
    val type: String = "", // "STOCK" or "SALE"
    val itemName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val profit: Double = 0.0,
    val date: Long = 0L
)
