plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    id("kotlin-kapt") // Добавляем плагин для поддержки kapt
}

android {
    namespace = "com.example.datingsmephi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.datingsmephi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Последняя стабильная версия
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    implementation("androidx.compose.runtime:runtime:1.5.3")
    implementation("androidx.compose.material:material-icons-extended:1.5.3")
    implementation("androidx.compose.foundation:foundation:1.5.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("com.github.bumptech.glide:glide:4.13.2")
    kapt("com.github.bumptech.glide:compiler:4.13.2") // Изменено на kapt

    // Retrofit и OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.30.1")

    implementation("androidx.compose.animation:animation:1.5.3")

    // Screen Navigation
    implementation("androidx.navigation:navigation-compose:2.7.3")

    // UI компоненты Material3
    implementation("androidx.compose.material3:material3:1.0.0")

    // Для Preview и Debugging
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.0")

    // Debugging для UI
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.3")

    // Activity для Compose
    implementation("androidx.activity:activity-compose:1.7.2")

    // Core библиотеки Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Для Room Database
    implementation("androidx.room:room-runtime:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0") // Изменено на kapt
    implementation("androidx.room:room-ktx:2.6.0")

    // Work Manager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Material Design
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha03")
    implementation("androidx.browser:browser:1.6.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1") // Изменено на kapt

    // Для Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Последняя версия JSON-сериализации

    // Тестирование
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.3")
}
