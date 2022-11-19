plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
}

android {
    namespace = "com.boolder.boolder"
    compileSdk = 32

    defaultConfig {
        applicationId = "com.boolder.boolder"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        buildFeatures {
            viewBinding = true
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "$project.rootDir/tools/proguard-rules-debug.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Algolia Search
    implementation("com.algolia:algoliasearch-client-kotlin:2.0.0")
    implementation("io.ktor:ktor-client-okhttp:2.1.3")

    //Mapbox
    implementation("com.mapbox.maps:android:10.9.1")

    // Play Services
    implementation("com.google.android.gms:play-services-location:20.0.0")

    // Database
    implementation("androidx.room:room-runtime:2.4.3")
    annotationProcessor("androidx.room:room-compiler:2.4.3")
    kapt("androidx.room:room-compiler:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")

    // DI
    implementation("io.insert-koin:koin-core:3.2.2")
    implementation("io.insert-koin:koin-android:3.2.2")


    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("io.insert-koin:koin-test-junit4:3.2.2")
    testImplementation("org.mockito:mockito-core:4.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}