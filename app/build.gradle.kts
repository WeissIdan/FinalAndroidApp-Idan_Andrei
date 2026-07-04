plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.finalapp_idan_andrei"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.finalapp_idan_andrei"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true // generates a *Binding class per layout (e.g. ActivityMainBinding)
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // Jetpack Navigation Component: drives the nav graph + hooks up the bottom nav bar.
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.recyclerview) // backs the History tab's list

    // Room: local SQLite database for saved speed test history + app settings.
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    // Generates AppDatabase's/SpeedTestDao's real implementation at compile time.
    annotationProcessor("androidx.room:room-compiler:$room_version")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}