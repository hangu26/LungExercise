# 🫁 LungExercise

> **호흡 훈련을 돕는 Android 앱**  
Wear OS와 연동하여 사용자의 호흡 운동을 측정하고, 시각적으로 피드백을 제공합니다.

---

## 📌 주요 기능

- 📊 **실시간 호흡 데이터 시각화** (MPAndroidChart, ExoPlayer)
- ⏱️ **호흡 훈련 모드 지원** (Lottie 애니메이션)
- ⌚ **Wear OS 연동** (Google Play Services Wearable)
- 🗂️ **데이터 저장** (Room + ViewModel)
- 🔄 **의존성 주입(DI)** (Koin)
- 🎵 **오디오 피드백 제공** (Media3 ExoPlayer)

---

## 🛠️ 기술 스택

- **언어:** Kotlin  
- **아키텍처:** MVVM  
- **비동기 처리:** RxJava, Coroutines 일부  
- **데이터 관리:** Room, ViewModel, LiveData  
- **의존성 주입:** Koin  
- **UI:** Lottie, MPAndroidChart, Material Design  
- **Wearable 연동:** Google Play Services Wearable  

---

## ⚙️ Gradle 설정

`build.gradle.kts` 주요 설정:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "kr.daejeonuinversity.lungexercise"
    compileSdk = 35

    defaultConfig {
        applicationId = "kr.daejeonuinversity.lungexercise"
        minSdk = 27
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

