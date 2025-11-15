package com.example.courseapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.courseapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DatabaseHelper
    private lateinit var cloudinaryManager: CloudinaryManager
    private var currentUser: String? = null
    private var currentUserId: Int = 0

    companion object {
        private const val GALLERY_REQUEST_CODE = 200
        private const val CAMERA_REQUEST_CODE = 201
        private const val CAMERA_PERMISSION_REQUEST_CODE = 202
        private const val STORAGE_PERMISSION_REQUEST_CODE = 203
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        db = DatabaseHelper(requireContext())
        cloudinaryManager = CloudinaryManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser = requireActivity().intent.getStringExtra("user_email")

        // CloudinaryManager ініциализациясы
        cloudinaryManager = CloudinaryManager(requireContext())

        binding.profileImage.setOnClickListener {
            showImageSelectionDialog()
        }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            intent.putExtra("user", currentUser)
            startActivity(intent)
        }

        loadUserData()
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Галереядан таңдау", "Камерамен түсіру")

        AlertDialog.Builder(requireContext())
            .setTitle("Профиль суретін өзгерту")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkStoragePermissionAndSelectImage()
                    1 -> checkCameraPermissionAndTakePhoto()
                }
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    private fun checkStoragePermissionAndSelectImage() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            selectImageFromGallery()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePhotoWithCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoWithCamera()
                } else {
                    Toast.makeText(requireContext(), "Камера рұқсаты қажет", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImageFromGallery()
                } else {
                    Toast.makeText(requireContext(), "Галерея рұқсаты қажет", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun selectImageFromGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Галереяға қатынасу қатесі", Toast.LENGTH_SHORT).show()
            Log.e("ProfileFragment", "Галерея қатесі: ${e.message}")
        }
    }

    private fun takePhotoWithCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(requireContext(), "Камера қолжетімді емес", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Камераны ашу қатесі", Toast.LENGTH_SHORT).show()
            Log.e("ProfileFragment", "Камера қатесі: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { imageUri ->
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                            // Бұл жерде Log қосыңыз:
                            Log.d("ProfileFragment", "✅ Сурет таңдалды, өлшемі: ${bitmap.width}x${bitmap.height}")

                            val resizedBitmap = resizeBitmap(bitmap, 800, 800)
                            uploadProfileImage(resizedBitmap) // ← БҰЛ ФУНКЦИЯ ЖҰМЫС ІСТЕУІ КЕРЕК
                        } catch (e: Exception) {
                            Log.e("ProfileFragment", "❌ Сурет жүктеу қатесі: ${e.message}")
                        }
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let { bmp ->
                        // Суретті кішірейту
                        val resizedBitmap = resizeBitmap(bmp, 800, 800)
                        uploadProfileImage(resizedBitmap)
                    } ?: run {
                        Toast.makeText(requireContext(), "Сурет алынбады", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        if (width > maxWidth || height > maxHeight) {
            val ratio = width.toFloat() / height.toFloat()
            if (ratio > 1) {
                width = maxWidth
                height = (maxWidth / ratio).toInt()
            } else {
                height = maxHeight
                width = (maxHeight * ratio).toInt()
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
        return bitmap
    }

    private fun uploadProfileImage(bitmap: Bitmap) {
        binding.btnEditProfile.isEnabled = false
        binding.btnEditProfile.text = "Сурет жүктелуде..."

        // Прогресс бар екенін тексеру
        try {
            binding.progressBar.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("ProfileFragment", "ProgressBar қатесі: ${e.message}")
        }

        Log.d("ProfileFragment", "🟡 uploadProfileImage шақырылды")

        cloudinaryManager.uploadProfileImage(
            bitmap = bitmap,
            onSuccess = { imageUrl ->
                requireActivity().runOnUiThread {
                    Log.d("ProfileFragment", "✅ Сурет жүктелді: $imageUrl")
                    saveProfileImageToDatabase(imageUrl)
                }
            },
            onError = { error ->
                requireActivity().runOnUiThread {
                    binding.btnEditProfile.isEnabled = true
                    binding.btnEditProfile.text = "Профильді өзгерту"
                    try {
                        binding.progressBar.visibility = View.GONE
                    } catch (e: Exception) {}

                    Toast.makeText(requireContext(), "Сурет жүктеу қатесі: $error", Toast.LENGTH_LONG).show()
                    Log.e("ProfileFragment", "❌ Cloudinary қатесі: $error")
                }
            }
        )
    }

    private fun saveProfileImageToDatabase(imageUrl: String) {
        Thread {
            try {
                val success = db.updateUserProfileImage(currentUserId, imageUrl)

                requireActivity().runOnUiThread {
                    binding.btnEditProfile.isEnabled = true
                    binding.btnEditProfile.text = "Профильді өзгерту"
                    binding.progressBar.visibility = View.GONE

                    if (success) {
                        // Суретті дерекқордан қайта жүктеу
                        loadUserData()
                        Toast.makeText(requireContext(), "Профиль суреті сәтті жаңартылды!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Суретті сақтау қатесі!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    binding.btnEditProfile.isEnabled = true
                    binding.btnEditProfile.text = "Профильді өзгерту"
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Қате: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun loadUserData() {
        val userLoginOrEmail = currentUser ?: return
        Thread {
            try {
                val dbReadable = db.readableDatabase
                val cursor = dbReadable.rawQuery(
                    "SELECT id, username, email, profile_image FROM users WHERE username=? OR email=?",
                    arrayOf(userLoginOrEmail, userLoginOrEmail)
                )

                var username = "белгісіз"
                var email = "белгісіз"
                var profileImage: String? = null

                if (cursor.moveToFirst()) {
                    currentUserId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email"))

                    val imageIndex = cursor.getColumnIndex("profile_image")
                    if (imageIndex != -1 && !cursor.isNull(imageIndex)) {
                        profileImage = cursor.getString(imageIndex)
                    }
                }

                cursor.close()
                dbReadable.close()

                requireActivity().runOnUiThread {
                    binding.tvUserEmail.text = "Логин: $username"
                    binding.tvEmail.text = "Email: $email"

                    if (!profileImage.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profileImage)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(binding.profileImage)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Деректерді жүктеу қатесі: ${e.message}")
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}