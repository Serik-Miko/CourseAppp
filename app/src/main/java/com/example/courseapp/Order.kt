package com.example.courseapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val id: Int,
    val userId: Int,
    val orderDate: String,
    val totalAmount: Int,
    val status: String, // "pending", "completed", "cancelled"
    val items: List<CartItem>
) : Parcelable