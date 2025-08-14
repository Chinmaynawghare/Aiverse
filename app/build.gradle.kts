import java.util.Properties

// ✅ Top-level function to read local.properties safely
fun lp(key: String, fallback: String = ""): String {
    val lpFile = rootProject.file("local.properties")
    val localProps = Properties()
    if (lpFile.exists()) {
        lpFile.inputStream().use { localProps.load(it) }
    }
    return localProps.getProperty(key) ?: System.getenv(key) ?: fallback
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinKapt)

    alias(libs.plugins.hilt)
    alias(libs.plugins.googleServices)
}

android {
    namespace = "com.aiverse.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aiverse.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Inject API keys securely for debug build
        buildConfigField("String", "OPENAI_API_KEY", "\"${lp("OPENAI_API_KEY")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${lp("GEMINI_API_KEY")}\"")
    }

    buildTypes {
        debug {
            // Debug uses keys from local.properties
            isMinifyEnabled = false
        }
        release {
            // Release: never ship API keys in APK
            buildConfigField("String", "OPENAI_API_KEY", "\"\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"\"")

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.gson.converter)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}
