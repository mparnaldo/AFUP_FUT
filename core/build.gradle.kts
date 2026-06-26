plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
}

android {
    namespace = "com.afup.afupfut.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.play.services)
    
    // Firebase (Compartilhado via api)
    api(platform(libs.firebase.bom))
    api(libs.firebase.firestore)
    api(libs.firebase.auth)
    api(libs.firebase.messaging)
    api(libs.firebase.storage)
    
    // Compose (Compartilhado via api)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.navigation.compose)
    
    // Imagens e Credenciais (Compartilhado via api)
    api(libs.coil.compose)
    api(libs.coil.network)
    api(libs.play.services.auth)
    api(libs.androidx.credentials)
    api(libs.androidx.credentials.play.services.auth)
    api(libs.googleid)

    testImplementation(libs.junit)
}
