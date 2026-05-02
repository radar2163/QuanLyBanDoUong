import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        load(FileInputStream(f))
    }
}

fun escapeBuildConfigString(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"")

fun localProp(key: String, default: String = ""): String =
    escapeBuildConfigString(localProperties.getProperty(key, default))

android {
    namespace = "com.example.cuoiki"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.cuoiki"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SMTP_HOST", "\"${localProp("smtp.host", "smtp.gmail.com")}\"")
        buildConfigField("String", "SMTP_PORT", "\"${localProp("smtp.port", "587")}\"")
        buildConfigField("String", "SMTP_USERNAME", "\"${localProp("smtp.username")}\"")
        buildConfigField("String", "SMTP_PASSWORD", "\"${localProp("smtp.password")}\"")
        buildConfigField("String", "SMTP_FROM_EMAIL", "\"${localProp("smtp.from.email")}\"")
        buildConfigField("String", "VNPAY_TMN_CODE", "\"${localProp("vnpay.tmn.code")}\"")
        buildConfigField("String", "VNPAY_SECRET_KEY", "\"${localProp("vnpay.secret.key")}\"")
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
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ✅ AndroidX Annotations (cần cho @NonNull)
    implementation("androidx.annotation:annotation:1.7.1")

    // 🧩 Thêm thư viện Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // 🔥 Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")  // Firebase Storage cho ảnh

    // 📱 ZXing for QR code scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")

    // 📧 JavaMail API để gửi email tự động
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
}

// 🔥 Apply Google Services plugin ở cuối file (QUAN TRỌNG!)
apply(plugin = "com.google.gms.google-services")
