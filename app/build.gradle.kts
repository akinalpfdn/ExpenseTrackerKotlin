plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.expensetrackerkotlin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.akinalpfdn.expensetracker"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("../keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Navigation
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Date picker
    //noinspection UseTomlInstead,GradleDependency
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")
    
    // Preferences DataStore
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Room Database
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.room:room-runtime:2.6.1")
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.billing.ktx)
    //noinspection GradleDependency,KaptUsageInsteadOfKsp,UseTomlInstead
    kapt("androidx.room:room-compiler:2.6.1")
    
    // For charts and progress rings
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Google Play Billing
    //noinspection GradleDependency,UseTomlInstead
    implementation("com.android.billingclient:billing-ktx:6.1.0")

    // Kotlinx Serialization
    //noinspection UseTomlInstead
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}