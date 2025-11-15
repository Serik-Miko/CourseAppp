package com.example.courseapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CourseAppDB"
        private const val DATABASE_VERSION = 6

        // Кестелердің атаулары
        private const val TABLE_USERS = "users"
        private const val TABLE_COURSES = "courses"
        private const val TABLE_USER_ROLES = "user_roles"
        private const val TABLE_CART = "cart"
        private const val TABLE_PURCHASES = "purchases"

        // Users кестесінің бағандары
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_USER_STATUS = "user_status"

        private const val COLUMN_PURCHASE_ID = "purchase_id"
        private const val COLUMN_PURCHASE_DATE = "purchase_date"

        // Courses кестесінің бағандары
        private const val COLUMN_COURSE_ID = "course_id"
        private const val COLUMN_COURSE_TITLE = "title"
        private const val COLUMN_COURSE_INSTRUCTOR = "instructor"
        private const val COLUMN_COURSE_DESCRIPTION = "description"
        private const val COLUMN_COURSE_PRICE = "price"
        private const val COLUMN_COURSE_DURATION = "duration"
        private const val COLUMN_COURSE_CATEGORY = "category"
        private const val COLUMN_COURSE_IMAGE = "image"
        private const val COLUMN_COURSE_FEATURES = "features"
        private const val COLUMN_COURSE_IMAGE_DATA = "image_data"

        // User roles кестесінің бағандары
        private const val COLUMN_ROLE = "role"

        // Cart кестесінің бағандары
        private const val COLUMN_CART_ID = "cart_id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_QUANTITY = "quantity"

        // Жаңа бағандар
        private const val COLUMN_COURSE_IMAGE_URL = "image_url"
        private const val COLUMN_USER_PROFILE_IMAGE = "profile_image"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users кестесі
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_STATUS TEXT DEFAULT 'active',
                $COLUMN_USER_PROFILE_IMAGE TEXT
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

        // Courses кестесі
        val createCoursesTable = """
            CREATE TABLE $TABLE_COURSES (
                $COLUMN_COURSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COURSE_TITLE TEXT NOT NULL,
                $COLUMN_COURSE_INSTRUCTOR TEXT NOT NULL,
                $COLUMN_COURSE_DESCRIPTION TEXT,
                $COLUMN_COURSE_PRICE INTEGER NOT NULL,
                $COLUMN_COURSE_DURATION TEXT,
                $COLUMN_COURSE_CATEGORY TEXT,
                $COLUMN_COURSE_IMAGE TEXT,
                $COLUMN_COURSE_FEATURES TEXT,
                $COLUMN_COURSE_IMAGE_DATA BLOB,
                $COLUMN_COURSE_IMAGE_URL TEXT
            )
        """.trimIndent()
        db.execSQL(createCoursesTable)

        // User roles кестесі
        val createUserRolesTable = """
            CREATE TABLE $TABLE_USER_ROLES (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_ROLE TEXT DEFAULT 'user',
                FOREIGN KEY($COLUMN_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(createUserRolesTable)

        // Cart кестесі
        val createCartTable = """
            CREATE TABLE $TABLE_CART (
                $COLUMN_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_COURSE_ID INTEGER NOT NULL,
                $COLUMN_QUANTITY INTEGER DEFAULT 1,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID),
                FOREIGN KEY($COLUMN_COURSE_ID) REFERENCES $TABLE_COURSES($COLUMN_COURSE_ID)
            )
        """.trimIndent()
        db.execSQL(createCartTable)

        // Purchases кестесі
        val createPurchasesTable = """
            CREATE TABLE $TABLE_PURCHASES (
                $COLUMN_PURCHASE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_COURSE_ID INTEGER NOT NULL,
                $COLUMN_PURCHASE_DATE TEXT NOT NULL,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID),
                FOREIGN KEY($COLUMN_COURSE_ID) REFERENCES $TABLE_COURSES($COLUMN_COURSE_ID),
                UNIQUE($COLUMN_USER_ID, $COLUMN_COURSE_ID)
            )
        """.trimIndent()
        db.execSQL(createPurchasesTable)

        addDefaultUsers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            // Courses кестесіне жаңа баған қосу
            db.execSQL("ALTER TABLE $TABLE_COURSES ADD COLUMN $COLUMN_COURSE_IMAGE_URL TEXT")
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_PROFILE_IMAGE TEXT")
        } else {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PURCHASES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_ROLES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            onCreate(db)
        }
    }

    private fun addDefaultUsers(db: SQLiteDatabase) {
        val defaultUsers = listOf(
            arrayOf("admin", "admin@courseapp.kz", "admin123", "admin"),
            arrayOf("erasyl", "era@email.kz", "erasyl", "user"),
            arrayOf("muhtar", "muhtar@m.kz", "muhtar", "user"),
            arrayOf("arai", "arai@e.kz", "arai123", "user")
        )

        for (userData in defaultUsers) {
            try {
                val cursor = db.rawQuery(
                    "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?",
                    arrayOf(userData[0], userData[1])
                )

                val userExists = cursor.moveToFirst()
                cursor.close()

                if (!userExists) {
                    val userValues = ContentValues().apply {
                        put(COLUMN_USERNAME, userData[0])
                        put(COLUMN_EMAIL, userData[1])
                        put(COLUMN_PASSWORD, userData[2])
                        put(COLUMN_USER_STATUS, "active")
                    }

                    val userId = db.insert(TABLE_USERS, null, userValues)

                    if (userId != -1L) {
                        val roleValues = ContentValues().apply {
                            put(COLUMN_ID, userId)
                            put(COLUMN_ROLE, userData[3])
                        }
                        db.insert(TABLE_USER_ROLES, null, roleValues)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // === ҚОЛДАНУШЫ ФУНКЦИЯЛАРЫ ===

    fun getUserStatusByIdentifier(identifier: String): String {
        val db = readableDatabase
        val query = """
            SELECT $COLUMN_USER_STATUS FROM $TABLE_USERS 
            WHERE $COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?
        """.trimIndent()

        var cursor: Cursor? = null
        return try {
            cursor = db.rawQuery(query, arrayOf(identifier, identifier))
            if (cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(COLUMN_USER_STATUS)
                if (statusIndex != -1) {
                    cursor.getString(statusIndex) ?: "active"
                } else {
                    "active"
                }
            } else {
                "active"
            }
        } catch (e: Exception) {
            "active"
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun getUserRoleByIdentifier(identifier: String): String {
        val db = readableDatabase
        val query = """
            SELECT ur.$COLUMN_ROLE 
            FROM $TABLE_USERS u 
            JOIN $TABLE_USER_ROLES ur ON u.$COLUMN_ID = ur.$COLUMN_ID 
            WHERE u.$COLUMN_USERNAME = ? OR u.$COLUMN_EMAIL = ?
        """.trimIndent()

        var cursor: Cursor? = null
        return try {
            cursor = db.rawQuery(query, arrayOf(identifier, identifier))
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
            } else {
                "user"
            }
        } catch (e: Exception) {
            "user"
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun registerUser(username: String, email: String, password: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, username)
                put(COLUMN_EMAIL, email)
                put(COLUMN_PASSWORD, password)
                put(COLUMN_USER_STATUS, "active")
            }
            val result = db.insert(TABLE_USERS, null, values)

            if (result != -1L) {
                val roleValues = ContentValues().apply {
                    put(COLUMN_ID, result)
                    put(COLUMN_ROLE, "user")
                }
                db.insert(TABLE_USER_ROLES, null, roleValues)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun isUserValid(usernameOrEmail: String, password: String): Boolean {
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_USERS 
            WHERE ($COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?) 
            AND $COLUMN_PASSWORD = ? 
            AND $COLUMN_USER_STATUS = 'active'
        """.trimIndent()

        var cursor: Cursor? = null
        return try {
            cursor = db.rawQuery(query, arrayOf(usernameOrEmail, usernameOrEmail, password))
            cursor.moveToFirst()
        } catch (e: Exception) {
            false
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase

        val query = """
        SELECT u.*, ur.$COLUMN_ROLE 
        FROM $TABLE_USERS u 
        LEFT JOIN $TABLE_USER_ROLES ur ON u.$COLUMN_ID = ur.$COLUMN_ID
        ORDER BY u.$COLUMN_ID
    """.trimIndent()

        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(query, null)

            if (cursor.moveToFirst()) {
                do {
                    val idIndex = cursor.getColumnIndex(COLUMN_ID)
                    val usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME)
                    val emailIndex = cursor.getColumnIndex(COLUMN_EMAIL)
                    val roleIndex = cursor.getColumnIndex(COLUMN_ROLE)
                    val statusIndex = cursor.getColumnIndex(COLUMN_USER_STATUS)
                    val profileImageIndex = cursor.getColumnIndex(COLUMN_USER_PROFILE_IMAGE) // ✅ ЖАҢА

                    if (idIndex != -1 && usernameIndex != -1 && emailIndex != -1) {
                        val user = User(
                            id = cursor.getInt(idIndex),
                            username = cursor.getString(usernameIndex),
                            email = cursor.getString(emailIndex),
                            role = if (roleIndex != -1) cursor.getString(roleIndex) ?: "user" else "user",
                            status = if (statusIndex != -1) cursor.getString(statusIndex) ?: "active" else "active",
                            registrationDate = "2024-01-0${cursor.getInt(idIndex)}",
                            purchasedCourses = getUserPurchasedCoursesCount(cursor.getInt(idIndex)),
                            profileImage = if (profileImageIndex != -1 && !cursor.isNull(profileImageIndex)) {
                                cursor.getString(profileImageIndex) // ✅ ПРОФИЛЬ СУРЕТІ
                            } else {
                                null
                            }
                        )
                        users.add(user)
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return users
    }

    fun updateUserStatus(userId: Int, status: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_USER_STATUS, status)
            }
            val result = db.update(
                TABLE_USERS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(userId.toString())
            )
            result > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun updateUserRole(userId: Int, newRole: String): Boolean {
        val db = writableDatabase
        return try {
            db.delete(TABLE_USER_ROLES, "$COLUMN_ID = ?", arrayOf(userId.toString()))
            val values = ContentValues().apply {
                put(COLUMN_ID, userId)
                put(COLUMN_ROLE, newRole)
            }
            val result = db.insert(TABLE_USER_ROLES, null, values)
            result != -1L
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun getUserEmail(identifier: String): String? {
        val db = readableDatabase
        val query = """
            SELECT $COLUMN_EMAIL FROM $TABLE_USERS 
            WHERE $COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?
        """.trimIndent()

        var cursor: Cursor? = null
        var email: String? = null
        try {
            cursor = db.rawQuery(query, arrayOf(identifier, identifier))
            if (cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return email
    }

    fun isUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?",
            arrayOf(username)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?",
            arrayOf(email)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun updateUser(oldIdentifier: String, newUsername: String?, newEmail: String?, newPassword: String?): Boolean {
        val db = writableDatabase
        val values = ContentValues()

        if (!newUsername.isNullOrBlank()) values.put(COLUMN_USERNAME, newUsername)
        if (!newEmail.isNullOrBlank()) values.put(COLUMN_EMAIL, newEmail)
        if (!newPassword.isNullOrBlank()) values.put(COLUMN_PASSWORD, newPassword)

        val result = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?",
            arrayOf(oldIdentifier, oldIdentifier)
        )

        db.close()
        return result > 0
    }

    // === КУРС ФУНКЦИЯЛАРЫ ===

    fun addCourse(course: Course, imageData: ByteArray? = null): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_COURSE_TITLE, course.title)
                put(COLUMN_COURSE_INSTRUCTOR, course.instructor)
                put(COLUMN_COURSE_DESCRIPTION, course.description)
                put(COLUMN_COURSE_PRICE, course.price)
                put(COLUMN_COURSE_DURATION, course.duration)
                put(COLUMN_COURSE_CATEGORY, course.features.firstOrNull() ?: "Жалпы")
                put(COLUMN_COURSE_FEATURES, course.features.joinToString("|"))
                if (imageData != null) {
                    put(COLUMN_COURSE_IMAGE_DATA, imageData)
                }
                put(COLUMN_COURSE_IMAGE, "custom")
            }
            val result = db.insert(TABLE_COURSES, null, values)
            result != -1L
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun addCourseWithUrl(course: Course, imageUrl: String? = null): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_COURSE_TITLE, course.title)
                put(COLUMN_COURSE_INSTRUCTOR, course.instructor)
                put(COLUMN_COURSE_DESCRIPTION, course.description)
                put(COLUMN_COURSE_PRICE, course.price)
                put(COLUMN_COURSE_DURATION, course.duration)
                put(COLUMN_COURSE_CATEGORY, course.features.firstOrNull() ?: "Жалпы")
                put(COLUMN_COURSE_FEATURES, course.features.joinToString("|"))
                put(COLUMN_COURSE_IMAGE_URL, imageUrl)
                put(COLUMN_COURSE_IMAGE, if (imageUrl != null) "cloudinary" else "default")
            }
            val result = db.insert(TABLE_COURSES, null, values)
            result != -1L
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun getAllCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_COURSES ORDER BY $COLUMN_COURSE_ID", null)

        try {
            if (cursor.moveToFirst()) {
                do {
                    val courseIdIndex = cursor.getColumnIndex(COLUMN_COURSE_ID)
                    val titleIndex = cursor.getColumnIndex(COLUMN_COURSE_TITLE)
                    val instructorIndex = cursor.getColumnIndex(COLUMN_COURSE_INSTRUCTOR)
                    val descriptionIndex = cursor.getColumnIndex(COLUMN_COURSE_DESCRIPTION)
                    val priceIndex = cursor.getColumnIndex(COLUMN_COURSE_PRICE)
                    val durationIndex = cursor.getColumnIndex(COLUMN_COURSE_DURATION)
                    val featuresIndex = cursor.getColumnIndex(COLUMN_COURSE_FEATURES)
                    val imageUrlIndex = cursor.getColumnIndex(COLUMN_COURSE_IMAGE_URL) // ✅ ЖАҢА

                    if (courseIdIndex != -1 && titleIndex != -1 && instructorIndex != -1) {
                        val course = Course(
                            id = cursor.getInt(courseIdIndex),
                            title = cursor.getString(titleIndex),
                            instructor = cursor.getString(instructorIndex),
                            rating = 4.5f,
                            reviews = (1..100).random(),
                            duration = if (durationIndex != -1) cursor.getString(durationIndex) else "2 апта",
                            price = if (priceIndex != -1) cursor.getInt(priceIndex) else 0,
                            imageRes = R.drawable.ic_launcher_foreground,
                            description = if (descriptionIndex != -1) cursor.getString(descriptionIndex) else "",
                            features = if (featuresIndex != -1) cursor.getString(featuresIndex).split("|") else emptyList(),
                            imageUrl = if (imageUrlIndex != -1 && !cursor.isNull(imageUrlIndex)) {
                                cursor.getString(imageUrlIndex) // ✅ КУРС СУРЕТІ
                            } else {
                                null
                            }
                        )
                        courses.add(course)

                        // ✅ ДЕБАГ: Курс суретін тексеру
                        Log.d("DatabaseHelper", "📚 Курс: ${course.title} - Сурет: ${course.imageUrl}")
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
            db.close()
        }
        return courses
    }

    fun updateCourse(course: Course, imageUrl: String? = null): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_COURSE_TITLE, course.title)
                put(COLUMN_COURSE_INSTRUCTOR, course.instructor)
                put(COLUMN_COURSE_DESCRIPTION, course.description)
                put(COLUMN_COURSE_PRICE, course.price)
                put(COLUMN_COURSE_DURATION, course.duration)
                put(COLUMN_COURSE_CATEGORY, course.features.firstOrNull() ?: "Жалпы")
                put(COLUMN_COURSE_FEATURES, course.features.joinToString("|"))

                // ✅ СУРЕТ URL САҚТАУ (ЕГЕР NULL БОЛМАСА)
                if (imageUrl != null) {
                    put(COLUMN_COURSE_IMAGE_URL, imageUrl)
                    Log.d("DatabaseHelper", "📸 Курс суреті жаңартылуда: $imageUrl")
                } else {
                    Log.d("DatabaseHelper", "📸 Курс суреті өзгеріссіз қалады")
                }
            }

            val result = db.update(
                TABLE_COURSES,
                values,
                "$COLUMN_COURSE_ID = ?",
                arrayOf(course.id.toString())
            )

            Log.d("DatabaseHelper", "📚 Курс өзгертілді: ${course.title}, сурет: $imageUrl, нәтиже: $result")
            result > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "❌ Курсты өзгерту қатесі: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

    fun deleteCourse(courseId: Int): Boolean {
        val db = writableDatabase
        return try {
            val result = db.delete(
                TABLE_COURSES,
                "$COLUMN_COURSE_ID = ?",
                arrayOf(courseId.toString())
            )
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun checkCourseExists(courseId: Int): Boolean {
        val db = readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_COURSES WHERE $COLUMN_COURSE_ID = ?"
        var cursor: Cursor? = null
        return try {
            cursor = db.rawQuery(query, arrayOf(courseId.toString()))
            cursor.moveToFirst() && cursor.getInt(0) > 0
        } catch (e: Exception) {
            false
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun getAllCourseIds(): List<Int> {
        val ids = mutableListOf<Int>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_COURSE_ID FROM $TABLE_COURSES", null)
        try {
            if (cursor.moveToFirst()) {
                do {
                    val idIndex = cursor.getColumnIndex(COLUMN_COURSE_ID)
                    if (idIndex != -1) {
                        ids.add(cursor.getInt(idIndex))
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
            db.close()
        }
        return ids
    }

    fun searchCourses(query: String): List<Course> {
        val courses = mutableListOf<Course>()
        val db = readableDatabase
        val searchQuery = """
            SELECT * FROM $TABLE_COURSES 
            WHERE $COLUMN_COURSE_TITLE LIKE ? OR $COLUMN_COURSE_INSTRUCTOR LIKE ?
        """.trimIndent()

        val cursor = db.rawQuery(searchQuery, arrayOf("%$query%", "%$query%"))
        try {
            while (cursor.moveToNext()) {
                val course = Course(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_TITLE)),
                    instructor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_INSTRUCTOR)),
                    rating = 4.5f,
                    reviews = 10,
                    duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_DURATION)),
                    price = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_PRICE)),
                    imageRes = R.drawable.ic_launcher_foreground,
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_DESCRIPTION)),
                    features = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_FEATURES)).split("|")
                )
                courses.add(course)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
            db.close()
        }
        return courses
    }

    // === СЕБЕТ ФУНКЦИЯЛАРЫ ===

    fun getCartItems(userId: Int): List<CartItem> {
        val cartItems = mutableListOf<CartItem>()
        val db = readableDatabase
        val query = """
            SELECT c.$COLUMN_CART_ID, c.$COLUMN_QUANTITY,
                   crs.$COLUMN_COURSE_ID, crs.$COLUMN_COURSE_TITLE, crs.$COLUMN_COURSE_INSTRUCTOR, 
                   crs.$COLUMN_COURSE_DESCRIPTION, crs.$COLUMN_COURSE_PRICE, crs.$COLUMN_COURSE_DURATION,
                   crs.$COLUMN_COURSE_FEATURES
            FROM $TABLE_CART c 
            JOIN $TABLE_COURSES crs ON c.$COLUMN_COURSE_ID = crs.$COLUMN_COURSE_ID 
            WHERE c.$COLUMN_USER_ID = ?
        """.trimIndent()

        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(query, arrayOf(userId.toString()))
            if (cursor.moveToFirst()) {
                do {
                    val cartIdIndex = cursor.getColumnIndex(COLUMN_CART_ID)
                    val quantityIndex = cursor.getColumnIndex(COLUMN_QUANTITY)
                    val courseIdIndex = cursor.getColumnIndex(COLUMN_COURSE_ID)
                    val titleIndex = cursor.getColumnIndex(COLUMN_COURSE_TITLE)
                    val instructorIndex = cursor.getColumnIndex(COLUMN_COURSE_INSTRUCTOR)
                    val descriptionIndex = cursor.getColumnIndex(COLUMN_COURSE_DESCRIPTION)
                    val priceIndex = cursor.getColumnIndex(COLUMN_COURSE_PRICE)
                    val durationIndex = cursor.getColumnIndex(COLUMN_COURSE_DURATION)
                    val featuresIndex = cursor.getColumnIndex(COLUMN_COURSE_FEATURES)

                    if (cartIdIndex != -1 && courseIdIndex != -1 && titleIndex != -1 &&
                        instructorIndex != -1 && priceIndex != -1) {

                        val course = Course(
                            id = cursor.getInt(courseIdIndex),
                            title = cursor.getString(titleIndex),
                            instructor = cursor.getString(instructorIndex),
                            rating = 4.5f,
                            reviews = 10,
                            duration = if (durationIndex != -1) cursor.getString(durationIndex) else "2 апта",
                            price = cursor.getInt(priceIndex),
                            imageRes = R.drawable.ic_launcher_foreground,
                            description = if (descriptionIndex != -1) cursor.getString(descriptionIndex) else "",
                            features = if (featuresIndex != -1) cursor.getString(featuresIndex).split("|") else emptyList()
                        )

                        val cartItem = CartItem(
                            id = cursor.getInt(cartIdIndex),
                            userId = userId,
                            course = course,
                            quantity = if (quantityIndex != -1) cursor.getInt(quantityIndex) else 1
                        )
                        cartItems.add(cartItem)
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return cartItems
    }

    fun addToCart(userId: Int, courseId: Int): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_USER_ID, userId)
                put(COLUMN_COURSE_ID, courseId)
                put(COLUMN_QUANTITY, 1)
            }
            db.insert(TABLE_CART, null, values) > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun removeFromCart(cartId: Int): Boolean {
        val db = writableDatabase
        return try {
            val result = db.delete(
                TABLE_CART,
                "$COLUMN_CART_ID = ?",
                arrayOf(cartId.toString())
            )
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun clearCart(userId: Int): Boolean {
        val db = writableDatabase
        return try {
            val result = db.delete(
                TABLE_CART,
                "$COLUMN_USER_ID = ?",
                arrayOf(userId.toString())
            )
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // === САТЫП АЛУ ФУНКЦИЯЛАРЫ ===

    fun purchaseCourse(userId: Int, courseId: Int): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_USER_ID, userId)
                put(COLUMN_COURSE_ID, courseId)
                put(COLUMN_PURCHASE_DATE, System.currentTimeMillis().toString())
            }
            val result = db.insertWithOnConflict(
                TABLE_PURCHASES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )
            result != -1L
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    fun isCoursePurchased(userId: Int, courseId: Int): Boolean {
        val db = readableDatabase
        val query = """
            SELECT COUNT(*) FROM $TABLE_PURCHASES 
            WHERE $COLUMN_USER_ID = ? AND $COLUMN_COURSE_ID = ?
        """.trimIndent()

        var cursor: Cursor? = null
        return try {
            cursor = db.rawQuery(query, arrayOf(userId.toString(), courseId.toString()))
            cursor.moveToFirst() && cursor.getInt(0) > 0
        } catch (e: Exception) {
            false
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun getUserPurchasedCourses(userId: Int): List<Course> {
        val courses = mutableListOf<Course>()
        val db = readableDatabase
        val query = """
            SELECT c.* FROM $TABLE_COURSES c 
            JOIN $TABLE_PURCHASES p ON c.$COLUMN_COURSE_ID = p.$COLUMN_COURSE_ID 
            WHERE p.$COLUMN_USER_ID = ?
        """.trimIndent()

        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(query, arrayOf(userId.toString()))
            if (cursor.moveToFirst()) {
                do {
                    val courseIdIndex = cursor.getColumnIndex(COLUMN_COURSE_ID)
                    val titleIndex = cursor.getColumnIndex(COLUMN_COURSE_TITLE)
                    val instructorIndex = cursor.getColumnIndex(COLUMN_COURSE_INSTRUCTOR)
                    val descriptionIndex = cursor.getColumnIndex(COLUMN_COURSE_DESCRIPTION)
                    val priceIndex = cursor.getColumnIndex(COLUMN_COURSE_PRICE)
                    val durationIndex = cursor.getColumnIndex(COLUMN_COURSE_DURATION)
                    val featuresIndex = cursor.getColumnIndex(COLUMN_COURSE_FEATURES)

                    if (courseIdIndex != -1 && titleIndex != -1 && instructorIndex != -1) {
                        val course = Course(
                            id = cursor.getInt(courseIdIndex),
                            title = cursor.getString(titleIndex),
                            instructor = cursor.getString(instructorIndex),
                            rating = 4.5f,
                            reviews = (1..100).random(),
                            duration = if (durationIndex != -1) cursor.getString(durationIndex) else "2 апта",
                            price = if (priceIndex != -1) cursor.getInt(priceIndex) else 0,
                            imageRes = R.drawable.ic_launcher_foreground,
                            description = if (descriptionIndex != -1) cursor.getString(descriptionIndex) else "",
                            features = if (featuresIndex != -1) cursor.getString(featuresIndex).split("|") else emptyList()
                        )
                        courses.add(course)
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return courses
    }

    fun getUserPurchasedCoursesCount(userId: Int): Int {
        val db = readableDatabase
        val query = """
            SELECT COUNT(*) FROM $TABLE_PURCHASES 
            WHERE $COLUMN_USER_ID = ?
        """.trimIndent()

        var cursor: Cursor? = null
        return try {
            cursor = db.rawQuery(query, arrayOf(userId.toString()))
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        } catch (e: Exception) {
            0
        } finally {
            cursor?.close()
            db.close()
        }
    }

    // === ПРОФИЛЬ СУРЕТІ ФУНКЦИЯЛАРЫ ===

    fun updateUserProfileImage(userId: Int, imageUrl: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_USER_PROFILE_IMAGE, imageUrl)
            }
            val result = db.update(
                TABLE_USERS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(userId.toString())
            )

            Log.d("DatabaseHelper", "📸 Профиль суреті жаңартылды: userId=$userId, success=$result")
            result > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "❌ Профиль суретін жаңарту қатесі: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

    fun getUserProfileImage(userId: Int): String? {
        val db = readableDatabase
        var imageUrl: String? = null
        val cursor = db.rawQuery(
            "SELECT $COLUMN_USER_PROFILE_IMAGE FROM $TABLE_USERS WHERE $COLUMN_ID = ?",
            arrayOf(userId.toString())
        )
        try {
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(COLUMN_USER_PROFILE_IMAGE)
                if (index != -1 && !cursor.isNull(index)) {
                    imageUrl = cursor.getString(index)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
            db.close()
        }
        return imageUrl
    }

    fun getCourseImageUrl(courseId: Int): String? {
        val db = readableDatabase
        var imageUrl: String? = null
        val cursor = db.rawQuery(
            "SELECT $COLUMN_COURSE_IMAGE_URL FROM $TABLE_COURSES WHERE $COLUMN_COURSE_ID = ?",
            arrayOf(courseId.toString())
        )
        try {
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(COLUMN_COURSE_IMAGE_URL)
                if (index != -1 && !cursor.isNull(index)) {
                    imageUrl = cursor.getString(index)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
            db.close()
        }
        return imageUrl
    }

    // === БАСҚА ФУНКЦИЯЛАР ===

    fun searchUsers(query: String): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase
        val searchQuery = """
            SELECT u.*, ur.$COLUMN_ROLE 
            FROM $TABLE_USERS u 
            LEFT JOIN $TABLE_USER_ROLES ur ON u.$COLUMN_ID = ur.$COLUMN_ID
            WHERE u.$COLUMN_USERNAME LIKE ? OR u.$COLUMN_EMAIL LIKE ?
            ORDER BY u.$COLUMN_ID
        """.trimIndent()

        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(searchQuery, arrayOf("%$query%", "%$query%"))
            if (cursor.moveToFirst()) {
                do {
                    val idIndex = cursor.getColumnIndex(COLUMN_ID)
                    val usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME)
                    val emailIndex = cursor.getColumnIndex(COLUMN_EMAIL)
                    val roleIndex = cursor.getColumnIndex(COLUMN_ROLE)
                    val statusIndex = cursor.getColumnIndex(COLUMN_USER_STATUS)

                    if (idIndex != -1 && usernameIndex != -1 && emailIndex != -1) {
                        val user = User(
                            id = cursor.getInt(idIndex),
                            username = cursor.getString(usernameIndex),
                            email = cursor.getString(emailIndex),
                            role = if (roleIndex != -1) cursor.getString(roleIndex) ?: "user" else "user",
                            status = if (statusIndex != -1) cursor.getString(statusIndex) ?: "active" else "active",
                            registrationDate = "2024-01-0${cursor.getInt(idIndex)}",
                            purchasedCourses = getUserPurchasedCoursesCount(cursor.getInt(idIndex))
                        )
                        users.add(user)
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return users
    }

    fun hasUsers(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USERS", null)
        return try {
            cursor.moveToFirst() && cursor.getInt(0) > 0
        } catch (e: Exception) {
            false
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getUserRole(userId: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_ROLE FROM $TABLE_USER_ROLES WHERE $COLUMN_ID = ?",
            arrayOf(userId.toString())
        )
        return try {
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
            } else {
                "user"
            }
        } catch (e: Exception) {
            "user"
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getUserStats(userId: Int): UserStats {
        return UserStats(
            totalCourses = getUserPurchasedCoursesCount(userId),
            totalSpent = getUserPurchasedCoursesCount(userId) * 10000,
            joinedDate = "2024-10-01"
        )
    }

    fun getUserData(identifier: String): Pair<String?, String?> {
        val db = readableDatabase
        val query = """
            SELECT $COLUMN_USERNAME, $COLUMN_EMAIL FROM $TABLE_USERS 
            WHERE $COLUMN_USERNAME = ? OR $COLUMN_EMAIL = ?
        """.trimIndent()

        var cursor: Cursor? = null
        var username: String? = null
        var email: String? = null
        try {
            cursor = db.rawQuery(query, arrayOf(identifier, identifier))
            if (cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }
        return Pair(username, email)
    }
}