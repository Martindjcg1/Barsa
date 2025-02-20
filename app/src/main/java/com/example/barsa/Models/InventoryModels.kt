package com.example.barsa.Models

data class InventoryCategory(
    val id: Int,
    val name: String,
    val description: String,
    val iconResId: Int
)

data class InventoryItem(
    val id: Int,
    val description: String,
    val entries: Int,
    val exits: Int,
    val stock: Int,
    val imageUrl: String? = null // Solo para aranceles
)