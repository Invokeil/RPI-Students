import java.util.Properties
import java.util.Base64
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// local.properties-এ APPWRITE_ENDPOINT / APPWRITE_PROJECT_ID দিলে সেটা নিবে,
// না দিলে default (sgp.cloud.appwrite.io) — app তবুও build হবে।
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val appwriteEndpoint: String = localProps.getProperty("APPWRITE_ENDPOINT", "https://sgp.cloud.appwrite.io/v1")
val appwriteProjectId: String = localProps.getProperty("APPWRITE_PROJECT_ID", "6b091cf5002c71f31ffb")
val appName: String = (project.findProperty("APP_NAME") as String?) ?: "RPI Student"
val orgName: String = (project.findProperty("ORG_NAME") as String?) ?: "Rangpur Polytechnic Institute"

// CI (GitHub Actions) প্রতি push-এ GITHUB_RUN_NUMBER দেয় — OTA versionCode একঘেয়েভাবে বাড়ে।
// Local build-এ run number না থাকলে 1।
val runNumber: Int = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toIntOrNull()?.coerceAtLeast(1) ?: 1

// Release signing — GitHub Actions secrets থেকে (KEYSTORE_BASE64 + passwords)।
// Secrets না থাকলে debug keystore দিয়ে sign হবে (local quick-install)।
val keystoreB64: String? = System.getenv("KEYSTORE_BASE64")
val ksPass: String? = System.getenv("KEYSTORE_PASSWORD")
val ksAlias: String? = System.getenv("KEY_ALIAS")
val ksKeyPass: String? = System.getenv("KEY_PASSWORD")
val canSign = !keystoreB64.isNullOrBlank() && !ksPass.isNullOrBlank() && !ksAlias.isNullOrBlank() && !ksKeyPass.isNullOrBlank()
val keystoreFile: File? = if (canSign && keystoreB64 != null) {
    rootProject.layout.buildDirectory.file("signing/release.keystore").get().asFile.also {
        it.parentFile.mkdirs()
        it.writeBytes(Base64.getMimeDecoder().decode(keystoreB64))
    }
} else null

android {
    namespace = "com.rpi.student"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rpi.student"
        minSdk = 26          // Android 8.0 — Liquid Glass flat fallback
        targetSdk = 35       // Play Store requirement
        versionCode = runNumber
        versionName = "1.0.${runNumber}"

        resValue("string", "app_name", appName)
        resValue("string", "org_name", orgName)
        buildConfigField("String", "APP_NAME", "\"${appName}\"")
        buildConfigField("String", "ORG_NAME", "\"${orgName}\"")
        buildConfigField("String", "APPWRITE_ENDPOINT", "\"${appwriteEndpoint}\"")
        buildConfigField("String", "APPWRITE_PROJECT_ID", "\"${appwriteProjectId}\"")
        buildConfigField("String", "GITHUB_REPO", "\"Invokeil/RPI-Students\"")
    }

    signingConfigs {
        if (canSign && keystoreFile != null) {
            create("release") {
                storeFile = keystoreFile
                storePassword = ksPass
                keyAlias = ksAlias
                keyPassword = ksKeyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (canSign) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.core.splashscreen)

    // Local cache (offline-first)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Backend: Appwrite (DB + Auth + Realtime) — https://sgp.cloud.appwrite.io/v1
    implementation(libs.appwrite.sdk)

    implementation(libs.kotlinx.serialization.json)
}
