import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.serialization)
    alias(libs.plugins.android.safeArgs)
}

android {
    namespace = "com.muratcay.marti_test_case"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.muratcay.marti_test_case"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            buildConfigField(
                type = "String",
                name = "MAPS_API_KEY",
                value = "\"${getProperty("MAPS_API_KEY")}\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
}

fun getProperty(propertyName: String): String? {
    println("getProperty called for: $propertyName") // Yeni satır
    val propFile = rootProject.file("local.properties")
    if (propFile.exists()) {
        println("local.properties exists") // Yeni satır
        val props = Properties()
        propFile.inputStream().use { props.load(it) }
        val value = props.getProperty(propertyName)
        println("Value from local.properties: $value") // Yeni satır
        return value
    } else {
        println("local.properties does NOT exist") // Yeni satır
        // local.properties yoksa, gradle.properties'den çekmeyi dene
        val value = System.getenv(propertyName) ?: project.properties[propertyName] as? String
        println("Value from gradle.properties or env: $value") // Yeni satır
        return value
    }
}

dependencies {

    implementation(project(":presentation"))
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.glide)

    implementation(libs.timber)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.google.maps)
    implementation(libs.maps.utils.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}