plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.j5ev.chargecalculator"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.j5ev.chargecalculator"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
