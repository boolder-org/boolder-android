plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("androidx.navigation.safeargs.kotlin")
    id("androidx.room")
}

android {
    namespace = "com.boolder.boolder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.boolder.boolder"
        minSdk = 21
        targetSdk = 34
        versionCode = 27 // bump when new version
        versionName = "1.21.0" // bump when new version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue(
            type = "string",
            name = "mapbox_access_token",
            value = providers.gradleProperty("MAPBOX_ACCESS_TOKEN").get()
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "$project.rootDir/tools/proguard-rules-debug.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //Ktor
    implementation("io.ktor:ktor-client-okhttp:2.1.3")
    implementation("io.ktor:ktor-client-resources:2.1.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.3")
    implementation("io.ktor:ktor-client-logging:2.1.3")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")

    //Mapbox
    implementation("com.mapbox.maps:android:11.3.0")

    // Play Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // DI
    implementation("io.insert-koin:koin-android:3.3.3")

    // Coil
    implementation("io.coil-kt:coil:2.5.0")

    // Jetpack compose
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-viewbinding")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("io.insert-koin:koin-test-junit4:3.3.3")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // Work manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
