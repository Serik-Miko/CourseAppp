package com.example.courseapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.courseapp.databinding.ActivityAddCourseBinding
import android.util.Log
import com.bumptech.glide.Glide

class AddCourseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCourseBinding
    private lateinit var db: DatabaseHelper
    private lateinit var cloudinaryManager: CloudinaryManager
    private var selectedImageBitmap: Bitmap? = null
    private var isEditMode = false
    private var courseId = 0

    private val categories = listOf("Мобилография", "Android Әзірлеу", "Веб-Әзірлеу", "Дизайн", "Маркетинг", "SMM")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // CloudinaryManager ініциализациясы
        cloudinaryManager = CloudinaryManager(this)

        binding = ActivityAddCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        cloudinaryManager = CloudinaryManager(this)
        setupCategorySpinner()
        checkEditMode()
        setupValidationListeners()

        binding.progressBar.visibility = View.GONE

        binding.btnSelectImage.setOnClickListener {
            showImageSelectionDialog()
        }

        binding.btnSaveCourse.setOnClickListener {
            if (isEditMode) {
                updateCourseInDatabase()
            } else {
                addCourseToDatabase()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupValidationListeners() {
        // Курс атауы тексеруі
        binding.etCourseTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateCourseTitle()
        }

        // Оқытушы тексеруі
        binding.etInstructor.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateInstructor()
        }

        // Баға тексеруі
        binding.etPrice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validatePrice()
        }

        // Ұзақтық тексеруі
        binding.etDuration.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateDuration()
        }

        // Сипаттама тексеруі
        binding.etDescription.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateDescription()
        }

        // Мүмкіндіктер тексеруі
        binding.etFeatures.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateFeatures()
        }
    }

    private fun validateCourseTitle(): Boolean {
        val title = binding.etCourseTitle.text.toString().trim()
        val result = Validator.validateCourseTitle(title)

        if (result.isValid) {
            binding.etCourseTitle.background = ContextCompat.getDrawable(this, R.drawable.input_bg)
            showFieldError(binding.etCourseTitle, null)
        } else {
            binding.etCourseTitle.background = ContextCompat.getDrawable(this, R.drawable.input_bg_error)
            showFieldError(binding.etCourseTitle, result.message)
        }

        return result.isValid
    }

    private fun validateInstructor(): Boolean {
        val instructor = binding.etInstructor.text.toString().trim()
        val result = Validator.validateInstructorName(instructor)

        if (result.isValid) {
            binding.etInstructor.background = ContextCompat.getDrawable(this, R.drawable.input_bg)
            showFieldError(binding.etInstructor, null)
        } else {
            binding.etInstructor.background = ContextCompat.getDrawable(this, R.drawable.input_bg_error)
            showFieldError(binding.etInstructor, result.message)
        }

        return result.isValid
    }

    private fun validatePrice(): Boolean {
        val price = binding.etPrice.text.toString().trim()
        val result = Validator.validatePrice(price)

        if (result.isValid) {
            binding.etPrice.background = ContextCompat.getDrawable(this, R.drawable.input_bg)
            showFieldError(binding.etPrice, null)
        } else {
            binding.etPrice.background = ContextCompat.getDrawable(this, R.drawable.input_bg_error)
            showFieldError(binding.etPrice, result.message)
        }

        return result.isValid
    }

    private fun validateDuration(): Boolean {
        val duration = binding.etDuration.text.toString().trim()
        val result = Validator.validateDuration(duration)

        if (result.isValid) {
            binding.etDuration.background = ContextCompat.getDrawable(this, R.drawable.input_bg)
            showFieldError(binding.etDuration, null)
        } else {
            binding.etDuration.background = ContextCompat.getDrawable(this, R.drawable.input_bg_error)
            showFieldError(binding.etDuration, result.message)
        }

        return result.isValid
    }

    private fun validateDescription(): Boolean {
        val description = binding.etDescription.text.toString().trim()
        val result = Validator.validateDescription(description)

        if (result.isValid) {
            binding.etDescription.background = ContextCompat.getDrawable(this, R.drawable.input_bg)
            showFieldError(binding.etDescription, null)
        } else {
            binding.etDescription.background = ContextCompat.getDrawable(this, R.drawable.input_bg_error)
            showFieldError(binding.etDescription, result.message)
        }

        return result.isValid
    }

    private fun validateFeatures(): Boolean {
        val features = binding.etFeatures.text.toString().trim()
        val result = Validator.validateFeatures(features)

        if (result.isValid) {
            binding.etFeatures.background = ContextCompat.getDrawable(this, R.drawable.input_bg)
            showFieldError(binding.etFeatures, null)
        } else {
            binding.etFeatures.background = ContextCompat.getDrawable(this, R.drawable.input_bg_error)
            showFieldError(binding.etFeatures, result.message)
        }

        return result.isValid
    }

    private fun showFieldError(view: android.widget.EditText, errorMessage: String?) {
        if (errorMessage != null) {
            view.error = errorMessage
        } else {
            view.error = null
        }
    }

    private fun validateAllFields(): Boolean {
        val validations = listOf(
            validateCourseTitle(),
            validateInstructor(),
            validatePrice(),
            validateDuration(),
            validateDescription(),
            validateFeatures()
        )

        if (validations.all { it }) {
            return true
        } else {
            // Бірінші қателікке фокус қою
            when {
                !validateCourseTitle() -> binding.etCourseTitle.requestFocus()
                !validateInstructor() -> binding.etInstructor.requestFocus()
                !validatePrice() -> binding.etPrice.requestFocus()
                !validateDuration() -> binding.etDuration.requestFocus()
                !validateDescription() -> binding.etDescription.requestFocus()
                !validateFeatures() -> binding.etFeatures.requestFocus()
            }
            return false
        }
    }

    private fun checkEditMode() {
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        if (isEditMode) {
            binding.tvTitle.text = "Курсты Өзгерту"
            courseId = intent.getIntExtra("COURSE_ID", 0)

            binding.etCourseTitle.setText(intent.getStringExtra("COURSE_TITLE"))
            binding.etInstructor.setText(intent.getStringExtra("COURSE_INSTRUCTOR"))
            binding.etPrice.setText(intent.getIntExtra("COURSE_PRICE", 0).toString())
            binding.etDuration.setText(intent.getStringExtra("COURSE_DURATION"))
            binding.etDescription.setText(intent.getStringExtra("COURSE_DESCRIPTION"))

            val features = intent.getStringExtra("COURSE_FEATURES")
            binding.etFeatures.setText(features)

            binding.btnSaveCourse.text = "Өзгерістерді Сақтау"

            // ✅ КУРС СУРЕТІН ЖҮКТЕУ (ЖАҢА)
            val imageUrl = intent.getStringExtra("COURSE_IMAGE_URL")
            Log.d("AddCourseActivity", "📸 Курс суреті алынуда: $imageUrl")
            if (!imageUrl.isNullOrEmpty()) {
                Log.d("AddCourseActivity", "📸 Курс суреті жүктелуде: $imageUrl")
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(binding.ivCourseImage)
            } else {
                Log.d("AddCourseActivity", "📸 Курс суреті жоқ")
            }
        } else {
            binding.tvTitle.text = "Жаңа Курс Қосу"
            binding.btnSaveCourse.text = "Сақтау"
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Галереядан таңдау", "Камерамен түсіру")

        AlertDialog.Builder(this)
            .setTitle("Сурет таңдау")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> selectImageFromGallery()
                    1 -> takePhotoWithCamera()
                }
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun takePhotoWithCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Камера қолжетімді емес", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { imageUri ->
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                            selectedImageBitmap = bitmap
                            binding.ivCourseImage.setImageBitmap(bitmap)
                            Toast.makeText(this, "Сурет таңдалды", Toast.LENGTH_SHORT).show()
                            Log.d("AddCourseActivity", "📸 Жаңа сурет таңдалды")
                        } catch (e: Exception) {
                            Toast.makeText(this, "Сурет жүктеу қатесі", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        selectedImageBitmap = it
                        binding.ivCourseImage.setImageBitmap(it)
                        Toast.makeText(this, "Сурет түсірілді", Toast.LENGTH_SHORT).show()
                        Log.d("AddCourseActivity", "📸 Камерамен сурет түсірілді")
                    }
                }
            }
        }
    }

    private fun addCourseToDatabase() {
        if (!validateAllFields()) {
            Toast.makeText(this, "Барлық өрістерді дұрыс толтырыңыз!", Toast.LENGTH_LONG).show()
            return
        }

        val title = binding.etCourseTitle.text.toString().trim()
        val instructor = binding.etInstructor.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val duration = binding.etDuration.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val features = binding.etFeatures.text.toString().trim()

        val course = Course(
            id = if (isEditMode) courseId else 0,
            title = title,
            instructor = instructor,
            rating = 0.0f,
            reviews = 0,
            duration = duration,
            price = price.toIntOrNull() ?: 0,
            imageRes = R.drawable.ic_launcher_foreground,
            description = description,
            features = if (features.isNotEmpty()) features.split("\n").filter { it.isNotBlank() } else listOf(category)
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveCourse.isEnabled = false
        binding.btnSaveCourse.text = "Сурет жүктелуде..."

        // ✅ ЕГЕР СУРЕТ ӨЗГЕРГЕН БОЛСА, ЖАҢА СУРЕТТІ ЖҮКТЕУ
        if (selectedImageBitmap != null) {
            Log.d("AddCourseActivity", "📸 Жаңа сурет жүктелуде...")
            cloudinaryManager.uploadCourseImage(
                bitmap = selectedImageBitmap!!,
                onSuccess = { imageUrl ->
                    runOnUiThread {
                        Log.d("AddCourseActivity", "✅ Жаңа сурет жүктелді: $imageUrl")
                        saveCourseToDatabase(course, imageUrl)
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveCourse.isEnabled = true
                        binding.btnSaveCourse.text = if (isEditMode) "Өзгерістерді Сақтау" else "Сақтау"

                        Toast.makeText(this, "Сурет жүктеу қатесі: $error", Toast.LENGTH_LONG).show()
                        Log.e("AddCourseActivity", "❌ Сурет жүктеу қатесі: $error")
                    }
                }
            )
        }
        // ✅ ЕГЕР СУРЕТ ӨЗГЕРМЕГЕН БОЛСА, ЕСКІ СУРЕТТІ САҚТАУ
        else if (isEditMode) {
            val oldImageUrl = intent.getStringExtra("COURSE_IMAGE_URL")
            Log.d("AddCourseActivity", "📸 Сурет өзгермеген, ескі сурет сақталуда: $oldImageUrl")
            saveCourseToDatabase(course, oldImageUrl)
        }
        // ✅ ЖАҢА КУРС ҚОСУДА СУРЕТ ЖОҚ БОЛСА
        else {
            Log.d("AddCourseActivity", "📚 Курс қосылуда (суретсіз): $title")
            saveCourseToDatabase(course, null)
        }
    }

    private fun saveCourseToDatabase(course: Course, imageUrl: String?) {
        Thread {
            try {
                val success = if (isEditMode) {
                    db.updateCourse(course, imageUrl) // ✅ СУРЕТ URL ЖІБЕРУ
                } else {
                    db.addCourseWithUrl(course, imageUrl)
                }

                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveCourse.isEnabled = true
                    binding.btnSaveCourse.text = if (isEditMode) "Өзгерістерді Сақтау" else "Сақтау"

                    if (success) {
                        Toast.makeText(this,
                            if (isEditMode) "Курс сәтті өзгертілді!" else "Курс сәтті қосылды!",
                            Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this,
                            if (isEditMode) "Курсты өзгерту кезінде қате!" else "Курсты қосу кезінде қате!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveCourse.isEnabled = true
                    binding.btnSaveCourse.text = if (isEditMode) "Өзгерістерді Сақтау" else "Сақтау"
                    Toast.makeText(this, "Қате: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }



    private fun updateCourseInDatabase() {
        if (!validateAllFields()) {
            Toast.makeText(this, "Барлық өрістерді дұрыс толтырыңыз!", Toast.LENGTH_LONG).show()
            return
        }

        val title = binding.etCourseTitle.text.toString().trim()
        val instructor = binding.etInstructor.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val duration = binding.etDuration.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val features = binding.etFeatures.text.toString().trim()

        val course = Course(
            id = courseId,
            title = title,
            instructor = instructor,
            rating = 0.0f,
            reviews = 0,
            duration = duration,
            price = price.toIntOrNull() ?: 0,
            imageRes = R.drawable.ic_launcher_foreground,
            description = description,
            features = if (features.isNotEmpty()) features.split("\n").filter { it.isNotBlank() } else listOf(category)
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveCourse.isEnabled = false
        binding.btnSaveCourse.text = "Жүктелуде..."

        // ✅ ЕГЕР СУРЕТ ӨЗГЕРГЕН БОЛСА, ЖАҢА СУРЕТТІ ЖҮКТЕУ
        if (selectedImageBitmap != null) {
            Log.d("AddCourseActivity", "📸 Жаңа сурет жүктелуде...")
            cloudinaryManager.uploadCourseImage(
                bitmap = selectedImageBitmap!!,
                onSuccess = { imageUrl ->
                    runOnUiThread {
                        Log.d("AddCourseActivity", "✅ Жаңа сурет жүктелді: $imageUrl")
                        // Сурет URL-мен бірге курсты сақтау
                        saveCourseToDatabase(course, imageUrl)
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveCourse.isEnabled = true
                        binding.btnSaveCourse.text = "Өзгерістерді Сақтау"
                        Toast.makeText(this, "Сурет жүктеу қатесі: $error", Toast.LENGTH_LONG).show()
                        Log.e("AddCourseActivity", "❌ Сурет жүктеу қатесі: $error")
                    }
                }
            )
        }
        // ✅ ЕГЕР СУРЕТ ӨЗГЕРМЕГЕН БОЛСА, ЕСКІ СУРЕТТІ САҚТАУ
        else {
            val oldImageUrl = intent.getStringExtra("COURSE_IMAGE_URL")
            Log.d("AddCourseActivity", "📸 Сурет өзгермеген, ескі сурет сақталуда: $oldImageUrl")
            // Ескі сурет URL-мен бірге курсты сақтау
            saveCourseToDatabase(course, oldImageUrl)
        }
    }

    private fun isCourseTitleExists(title: String): Boolean {
        val allCourses = db.getAllCourses()
        return allCourses.any { course -> course.title.equals(title, ignoreCase = true) }
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
    }
}