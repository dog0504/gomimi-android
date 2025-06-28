import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.test"
    compileSdk = 35

    buildFeatures {
        buildConfig = true // BuildConfig機能を有効にする
        viewBinding = true
    }

    // local.propertiesから値を読み込むための設定
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties") // local.propertiesファイルへの参照を取得
    if (localPropertiesFile.exists()) { // ファイルが存在するか確認
        localProperties.load(FileInputStream(localPropertiesFile)) // ファイルの内容を読み込む
    }

    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfigにフィールドを追加
        // localProperties.getProperty("キー名", "デフォルト値") で値を取得
        buildConfigField(
            "String",
            "API_ENDPOINT",
            "\"${localProperties.getProperty("API_ENDPOINT", "https://default-api.example.com/")}\"" // デフォルト値も設定
        )
        buildConfigField(
            "String",
            "MY_API_KEY",
            "\"${localProperties.getProperty("MY_API_KEY", "default_api_key")}\"" // デフォルト値も設定
        )

    }

    buildTypes {
        getByName("release") {
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
}

dependencies {
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.android.gms:play-services-auth:21.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    val cameraxVersion = "1.4.2"
    implementation ("androidx.camera:camera-core:${cameraxVersion}")
    implementation ("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation ("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation ("androidx.camera:camera-video:${cameraxVersion}")

    implementation ("androidx.camera:camera-view:${cameraxVersion}")
    implementation ("androidx.camera:camera-extensions:${cameraxVersion}")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    implementation("com.orhanobut:hawk:2.0.1")

    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
