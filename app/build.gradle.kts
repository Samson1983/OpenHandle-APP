plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
    id ("com.yanzhenjie.andserver")
}

android {
    namespace = "com.addon.hid.ble"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.addon.hid.ble"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(files("../libs/hid-sdk-release.aar"))
    // 蓝牙通信库 (RxAndroidBle)
    // 基于RxJava的Android蓝牙低功耗(BLE)通信库
    // 版本：1.18.1
    implementation ("com.polidea.rxandroidble3:rxandroidble:1.18.1")

    // AndServer 服务器库 (核心API)
    // 允许在Android设备上创建HTTP服务器的库
    // 版本：2.1.12
    implementation ("com.yanzhenjie.andserver:api:2.1.12")

    // AndServer 注解处理器
    // 用于处理AndServer中的注解(如@RestController等)
    // 需要与kapt配合使用
    // 版本：2.1.12
    kapt ("com.yanzhenjie.andserver:processor:2.1.12")

    // Google的Gson库
    // 用于JSON数据的序列化和反序列化
    // 版本：2.10.1
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}