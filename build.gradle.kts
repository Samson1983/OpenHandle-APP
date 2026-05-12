// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    kotlin("kapt") version "1.9.0"
}

buildscript {
    dependencies {
        classpath("com.yanzhenjie.andserver:plugin:2.1.12")
        classpath ("com.android.tools.build:gradle:4.2.2")
    }
}
