package com.example.courseapp

import java.io.Serializable

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String = "user", // "admin" немесе "user"
    val status: String = "active", // "active" немесе "blocked"
    val registrationDate: String,
    val lastLogin: String? = null,
    val purchasedCourses: Int = 0,
    val profileImage: String? = null // ✅ ЖАҢА: Cloudinary URL
) : Serializable