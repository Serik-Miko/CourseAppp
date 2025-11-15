plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.courseapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.courseapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Layout (XML) қолданамыз, сондықтан ViewBinding қосамыз
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // ✅ GLIDE бар екенін тексеру
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ✅ Cloudinary қосамыз
    implementation("com.cloudinary:cloudinary-android:2.3.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Сурет жүктеу үшін қосымша кітапханалар
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // Камера және галерея үшін
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // Негізгі Android кітапханалар
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ✅ ҚОСЫМША КІТАПХАНАЛАР (курстар үшін)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // ✅ Parcelize үшін қажет
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

    // Тесттер
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
