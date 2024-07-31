plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}

// build.gradle.kts

buildscript {
    repositories {
        google() // Adds the Google Maven repository
        mavenCentral() // Adds Maven Central repository
    }
    dependencies {
        // Classpath dependencies for the buildscript
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath(kotlin("gradle-plugin", version = "1.5.21"))
    }
}

allprojects {
    repositories {
        google() // Adds the Google Maven repository for all modules
        mavenCentral() // Adds Maven Central repository for all modules
    }
}

