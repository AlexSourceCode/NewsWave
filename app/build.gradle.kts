plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.newswave"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.newswave"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("com.google.android.material:material:1.6.0")

    implementation("com.squareup.picasso:picasso:2.8")
    implementation("io.coil-kt:coil-compose:2.7.0")







    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")

    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

//    implementation("androidx.navigation.ui.setupWithNavController")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    implementation("androidx.media3:media3-exoplayer:1.0.0")
    implementation("androidx.media3:media3-ui:1.0.0")


    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.0")
    implementation("com.google.dagger:dagger-android:2.51")
    implementation("com.google.dagger:dagger:2.51")
    kapt("com.google.dagger:dagger-compiler:2.51")

    implementation("androidx.work:work-runtime:2.9.0")

    implementation("com.google.dagger:dagger-android:2.51")
    implementation("com.google.dagger:dagger:2.51")
    kapt("com.google.dagger:dagger-compiler:2.51")
    implementation("com.squareup.inject:assisted-inject-annotations-dagger2:0.8.1")
    kapt("com.squareup.inject:assisted-inject-processor-dagger2:0.8.1")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")







}