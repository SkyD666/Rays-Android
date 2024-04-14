import com.android.build.api.variant.FilterConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("kotlin-parcelize")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

apply(from = "../secret.gradle.kts")

android {
    namespace = "com.skyd.rays"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.skyd.rays"
        minSdk = 24
        targetSdk = 34
        versionCode = 65
        versionName = "2.2-rc02"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    signingConfigs {
        create("release") {
            @Suppress("UNCHECKED_CAST")
            val sign = ((extra["secret"] as Map<*, *>)["sign"] as Map<String, String>)
            storeFile = file("../key.jks")
            storePassword = sign["RELEASE_STORE_PASSWORD"]
            keyAlias = sign["RELEASE_KEY_ALIAS"]
            keyPassword = sign["RELEASE_KEY_PASSWORD"]
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("GitHub") {
            dimension = "version"
        }
    }

    splits {
        abi {
            // Enables building multiple APKs per ABI.
            isEnable = true
            // By default all ABIs are included, so use reset() and include().
            // Resets the list of ABIs for Gradle to create APKs for to none.
            reset()
            // A list of ABIs for Gradle to create APKs for.
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            // We want to also generate a universal APK that includes all ABIs.
            isUniversalApk = true
        }
    }

    applicationVariants.all {
        outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val abi = output.getFilter(FilterConfiguration.FilterType.ABI.name) ?: "universal"
                output.outputFileName =
                    "Rays_${versionName}_${abi}_${buildType.name}_${flavorName}.apk"
            }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = ".debug"
        }
        release {
            signingConfig = signingConfigs.getByName("release")    // signing
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources.excludes += mutableSetOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "DebugProbesKt.bin",
            "kotlin-tooling-metadata.json",
            "okhttp3/internal/publicsuffix/NOTICE",
            "XPP3_1.1.3.3_VERSION",
            "XPP3_1.1.3.2_VERSION",
        )
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

tasks.withType(KotlinCompile::class.java).configureEach {
    //noinspection GrDeprecatedAPIUsage
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=coil.annotation.ExperimentalCoilApi",
            "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}

dependencies {
    val composeVersion: String by rootProject.extra
    val md3Version: String by rootProject.extra
    val accompanistVersion: String by rootProject.extra
    val mlkitRecognitionVersion: String by rootProject.extra
    val roomVersion: String by rootProject.extra
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:$md3Version")
    implementation("androidx.compose.material3:material3-window-size-class:$md3Version")
    implementation("androidx.compose.material:material:1.6.5")
    implementation("androidx.compose.material:material-icons-extended:1.6.5")
    implementation("androidx.compose.ui:ui-tooling-preview:$md3Version")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.accompanist:accompanist-drawablepainter:$accompanistVersion")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("com.github.thegrizzlylabs:sardine-android:0.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.materialkolor:material-kolor:1.4.4")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.airbnb.android:lottie-compose:6.3.0")

    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Google ML Kit
    implementation("com.google.mlkit:text-recognition:$mlkitRecognitionVersion")
    implementation("com.google.mlkit:text-recognition-chinese:$mlkitRecognitionVersion")
    implementation("com.google.mlkit:text-recognition-japanese:$mlkitRecognitionVersion")
    implementation("com.google.mlkit:text-recognition-korean:$mlkitRecognitionVersion")
    implementation("com.google.mlkit:image-labeling-custom:17.0.2")
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta5")

    // TF Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    implementation("com.github.penfeizhou.android.animation:apng:2.28.0")

    debugImplementation("androidx.compose.ui:ui-tooling:1.6.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.5")
}