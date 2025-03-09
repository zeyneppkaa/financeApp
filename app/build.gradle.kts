plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("androidx.navigation.safeargs.kotlin")
    id ("kotlin-kapt")
    id("com.google.gms.google-services")

    // Kotlin serialization plugin for type safe routes and navigation arguments
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.tez"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tez"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.firebase.firestore.ktx)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.googleid)
    val nav_version = "2.8.6"

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Jetpack Compose integration
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

}