import org.gradle.kotlin.dsl.provider.inClassPathMode

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.github.fontoura.sample.shoplist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.github.fontoura.sample.shoplist"
        minSdk = 26
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
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
        }
    }
}

dependencies {

    implementation(libs.annotation)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.converter.jackson)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)
    implementation(libs.material.v140)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}