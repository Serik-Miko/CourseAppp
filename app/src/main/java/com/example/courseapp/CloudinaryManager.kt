package com.example.courseapp

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import java.io.ByteArrayOutputStream
import java.util.UUID

class CloudinaryManager(private val context: Context) {

    private val cloudinary: Cloudinary

    init {
        val config = mapOf(
            "cloud_name" to "dtgbmyanq",
            "api_key" to "788423636289616",
            "api_secret" to "HJDcACQlX6Yc82OHzztUa67gouY",
            "secure" to true
        )
        cloudinary = Cloudinary(config)
        Log.d("CloudinaryManager", "✅ Cloudinary SDK бапталды")
    }

    fun uploadCourseImage(bitmap: Bitmap, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        uploadImage(bitmap, "courseapp/courses", onSuccess, onError)
    }

    fun uploadProfileImage(bitmap: Bitmap, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        uploadImage(bitmap, "courseapp/profiles", onSuccess, onError)
    }

    private fun uploadImage(bitmap: Bitmap, folder: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                Log.d("CloudinaryManager", "🟡 uploadImage басталды (Thread)")

                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val imageBytes = stream.toByteArray()

                Log.d("CloudinaryManager", "🟡 Cloudinary upload() шақырылады...")

                val uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap(
                        "folder", folder,
                        "public_id", "${folder}_${UUID.randomUUID()}",
                        "overwrite", true
                    )
                )

                Log.d("CloudinaryManager", "✅ Cloudinary жауабы: $uploadResult")

                val secureUrl = uploadResult["secure_url"] as? String
                if (secureUrl != null) {
                    Log.d("CloudinaryManager", "🔗 Сурет URL: $secureUrl")
                    onSuccess(secureUrl)
                } else {
                    Log.e("CloudinaryManager", "❌ URL алынбады")
                    onError("Сурет URL алынбады")
                }

            } catch (e: Exception) {
                Log.e("CloudinaryManager", "❌ Жүктеу қатесі: ${e.message}")
                e.printStackTrace()
                onError("Сурет жүктеу қатесі: ${e.message}")
            }
        }.start()
    }
}