plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.safehome.inventory"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.safehome.inventory"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "runtime"
    productFlavors {
        create("onnx") {
            dimension = "runtime"
            applicationIdSuffix = ".onnx"
            versionNameSuffix = "-onnx"
            manifestPlaceholders["appName"] = "SafeHome (ONNX)"
        }
        create("executorch") {
            dimension = "runtime"
            applicationIdSuffix = ".executorch"
            versionNameSuffix = "-executorch"
            manifestPlaceholders["appName"] = "SafeHome (ExecuTorch)"
        }
        create("qnn") {
            dimension = "runtime"
            applicationIdSuffix = ".qnn"
            versionNameSuffix = "-qnn"
            manifestPlaceholders["appName"] = "SafeHome (QNN)"
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

    packaging {
        // Pick first for duplicate native libraries from ExecuTorch vs PyTorch dependencies
        pickFirst("lib/arm64-v8a/libc++_shared.so")
        pickFirst("lib/armeabi-v7a/libc++_shared.so")
        pickFirst("lib/x86/libc++_shared.so")
        pickFirst("lib/x86_64/libc++_shared.so")
        pickFirst("lib/arm64-v8a/libfbjni.so")
        pickFirst("lib/armeabi-v7a/libfbjni.so")
        pickFirst("lib/x86/libfbjni.so")
        pickFirst("lib/x86_64/libfbjni.so")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ONNX flavor - ONNXRuntime
    add("onnxImplementation", "com.microsoft.onnxruntime:onnxruntime-android:1.18.0")

    // ExecuTorch flavor - using ONNX Runtime bridge until proper .pte export
    add("executorchImplementation", "com.microsoft.onnxruntime:onnxruntime-android:1.18.0")
    add("executorchImplementation", "org.pytorch:pytorch_android_lite:1.13.1")
    add("executorchImplementation", "org.pytorch:pytorch_android_torchvision_lite:1.13.1")

    // QNN flavor - Qualcomm QNN backend for maximum NPU performance
    add("qnnImplementation", "com.microsoft.onnxruntime:onnxruntime-android:1.18.0")
    add("qnnImplementation", "org.pytorch:pytorch_android_lite:1.13.1")
    add("qnnImplementation", "org.pytorch:pytorch_android_torchvision_lite:1.13.1")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ViewPager2 for presentation slides
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Kotlin Coroutines (for AI description generation)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
