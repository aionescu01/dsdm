plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.proiect_dsdm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.proiect_dsdm"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.core)
    implementation(libs.room.runtime)
    implementation(libs.google.services)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.recyclerview)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    annotationProcessor(libs.androidx.room.compiler)
}