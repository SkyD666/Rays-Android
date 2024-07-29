buildscript {
    extra.apply {
        set("md3Version", "1.2.1")
        set("mlkitRecognitionVersion", "16.0.0")
        set("roomVersion", "2.6.1")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.1" apply false
    id("com.android.library") version "8.5.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.23" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}
