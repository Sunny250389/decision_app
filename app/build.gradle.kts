plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.decisionapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.decisionapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    // --- OkHttp core ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // --- OkHttp SSE support ---
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")

    // --- JSON parsing (already used by you) ---
    implementation("org.json:json:20240303")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // StateFlow helpers (update, asStateFlow)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    debugImplementation("androidx.compose.ui:ui-tooling")
}