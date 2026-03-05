import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val kakaoKey = localProperties.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""
val geminiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: ""
val supabaseKey = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""

android {
    namespace = "com.example.arcanaai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.arcanaai"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKey\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
        
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoKey
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("com.kakao.sdk:v2-user:2.20.1")
    
    // Gemini
    implementation(libs.generativeai)

    // Supabase
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.gotrue)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)

    // 💰 Google Play Billing
    implementation(libs.billing.ktx)
    
    // 🎨 Extended Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    // 🖼️ Coil (Image Loading)
    implementation("io.coil-kt:coil-compose:2.6.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
