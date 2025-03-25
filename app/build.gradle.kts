import com.android.build.api.variant.FilterConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.objectbox)
}

apply(from = "../secret.gradle.kts")

android {
    namespace = "com.skyd.rays"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.skyd.rays"
        minSdk = 24
        targetSdk = 35
        versionCode = 68
        versionName = "2.4-alpha03"

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
    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }
}

tasks.withType(KotlinCompile::class.java).configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.window.size)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.icons)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.security.crypto)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.sardine.android) {
        exclude(group = "xpp3", module = "xpp3")
    }
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material.kolor)
    implementation(libs.lottie.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.telephoto.subsamplingimage)

    implementation(libs.retrofit2)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    // Google ML Kit
    implementation(libs.text.recognition)
    implementation(libs.text.recognition.chinese)
    implementation(libs.text.recognition.japanese)
    implementation(libs.text.recognition.korean)
    implementation(libs.image.labeling.custom)
    implementation(libs.segmentation.selfie)
    implementation(libs.tasks.vision)
    // TF Lite
    implementation(libs.ai.edge.litert)
    implementation(libs.ai.edge.litert.support)

    implementation(libs.apng)

    debugImplementation(libs.androidx.compose.ui.ui.tooling3)
    debugImplementation(libs.androidx.compose.ui.ui.test.manifest)

    testImplementation(libs.junit)
}