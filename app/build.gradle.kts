plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.group20.lbdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.group20.lbdemo"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- Retrofit + Gson ---
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // --- OkHttp Logging Interceptor ---
    implementation(libs.logging.interceptor)

    // --- Coroutines để chạy bất đồng bộ ---
    implementation(libs.kotlinx.coroutines.android)

    // --- LifecycleScope (để poll health trong Activity) ---
    implementation(libs.androidx.lifecycle.runtime.ktx)
}