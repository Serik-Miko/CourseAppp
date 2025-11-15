package com.example.courseapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val id: Int,
    val userId: Int,
    val course: Course,
    val quantity: Int = 1
) : Parcelable