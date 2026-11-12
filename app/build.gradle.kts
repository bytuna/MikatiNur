import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}
android {
    namespace = "com.example.mkat_nur"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mkat_nur"
        minSdk = 24
        targetSdk = 35
        versionCode = 10
        versionName = "1.1.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Kimlik bilgileri local.properties'ten okunur (git'e girmez).
            val props = Properties()
            val localProps = rootProject.file("local.properties")
            if (localProps.exists()) {
                localProps.inputStream().use { props.load(it) }
            }
            storeFile = props.getProperty("MIKATINUR_STORE_FILE")?.let { file(it) }
            storePassword = props.getProperty("MIKATINUR_STORE_PASSWORD")
            keyAlias = props.getProperty("MIKATINUR_KEY_ALIAS")
            keyPassword = props.getProperty("MIKATINUR_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")
    // Kur'an-ı Kerim 1: sesli tilavet
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")
    implementation("androidx.webkit:webkit:1.11.0")
    
    // NAVİGASYON
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // COIL (Görüntüler için)
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // FIREBASE BOM & AUTH & FIRESTORE
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // GPS KONUM
    implementation("com.google.android.gms:play-services-location:21.3.0")

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
