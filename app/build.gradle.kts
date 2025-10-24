plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.androidapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.androidapp"
        minSdk = 29
        targetSdk = 36
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
}

dependencies {
    // Các thư viện mặc định
    implementation(libs.appcompat)
    implementation(libs.material) // Giữ lại dòng này
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Các thư viện Firebase
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage) // Giữ lại dòng này
    implementation("com.google.firebase:firebase-database:20.3.0") // Có thể bạn dùng cho chức năng khác

    // Thư viện Glide (chọn phiên bản mới nhất 4.16.0)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Thư viện khác
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Dưới đây là các dòng đã bị xóa đi để tránh xung đột
    // implementation("com.google.android.material:material:1.9.0") // Đã xóa vì trùng
    // implementation ("com.google.firebase:firebase-storage") // Đã xóa vì trùng
    // implementation("com.squareup.picasso:picasso:2.8") // Đã xóa vì thừa
    // implementation("com.github.bumptech.glide:glide:4.15.1") // Đã xóa vì xung đột phiên bản
    // annotationProcessor("com.github.bumptech.glide:compiler:4.15.1") // Đã xóa vì xung đột phiên bản
}