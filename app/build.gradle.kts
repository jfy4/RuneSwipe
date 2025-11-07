// ─────────────────────────────────────────────────────────────────────────────
// app/build.gradle.kts
// ─────────────────────────────────────────────────────────────────────────────
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.runeswipe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.runeswipe"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Compose BOM keeps all Compose libs at matching versions
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core + Compose base
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // ✅ Material 3 (provides Theme.Material3.*)
    implementation("androidx.compose.material3:material3:1.3.0")

    // UI + tooling
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation (for Tome ↔ Battle screens)
    implementation("androidx.navigation:navigation-compose:2.8.2")
    
    // ONNX
    // implementation("com.microsoft.onnxruntime:onnxruntime-mobile:1.18.0")
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.18.0")

    // for persistant characters
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

}
