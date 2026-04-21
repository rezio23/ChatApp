package com.chatapp.model

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val purchasePrice: Double = 0.0
)
