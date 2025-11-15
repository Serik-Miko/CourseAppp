package com.example.courseapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val rating: Float,
    val reviews: Int,
    val duration: String,
    val price: Int,
    val imageRes: Int,
    val description: String,
    val features: List<String>,
    val isPurchased: Boolean = false,
    val imageUrl: String? = null // ✅ ЖАҢА: Cloudinary URL
) : Parcelable