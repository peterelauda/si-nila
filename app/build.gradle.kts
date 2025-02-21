plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eparrotfishnilabali"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.eparrotfishnilabali"
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

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview.v7)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.google.android.material:material:1.12.0")

    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-analytics")

    // Realtime Database
    implementation("com.google.firebase:firebase-database")

    // Firestore
    implementation ("com.google.firebase:firebase-firestore:25.1.0")

    // Authentication
    implementation("com.google.firebase:firebase-auth")

    // Storage
    implementation("com.google.firebase:firebase-storage")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    // WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.8.1")

    // MPAndroidChart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

}