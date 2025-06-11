plugins {
    alias(libs.plugins.android.application)


}

android {
    namespace = "com.example.arw"
    compileSdk = 35



    defaultConfig {
        applicationId = "com.example.arw"
        minSdk = 30
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation (libs.androidx.room.runtime)
    implementation(libs.image.labeling.custom.common)
//    implementation(libs.object1.detection.common)
//    implementation(libs.object1.detection)
//    implementation(libs.litert.support.api)
    implementation (libs.tensorflow.lite.task.vision)
    implementation (libs.object1.detection.v1700)
    annotationProcessor (libs.androidx.room.compiler)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.image.labeling.common)
    implementation(libs.image.labeling.default.common)
    implementation(libs.vision.common)
    implementation(libs.translate)
    implementation(libs.camera.core)
    implementation(libs.camera.view)
    implementation(libs.camera.lifecycle)
//    implementation(libs.room.common.jvm)
//    implementation(libs.room.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.image.labeling)
    implementation(libs.camerax.camera2)
    implementation  (libs.image.labeling.v1707)

}