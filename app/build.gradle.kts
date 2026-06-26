plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.googleServices)
}

android {
    namespace = "com.afup.afupfut"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.afup.afupfut"
        minSdk = 26
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core-ktx)
    implementation(libs.androidx.lifecycle.runtime-ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    
    // Imagens e Credenciais
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    implementation(libs.play-services-auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play-services-auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.play-services)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Tarefa para copiar o APK compilado para o diretório de APKs Gerados do usuário
tasks.register<Copy>("copyApkToGeneratedFolder") {
    from(layout.buildDirectory.dir("outputs/apk/debug"))
    include("app-debug.apk")
    into(file("../APKs Gerados"))
    rename { "AFUP_FUT-debug.apk" }
}

// Vincula a cópia do APK ao final do build de assembleDebug
tasks.named("assembleDebug") {
    finalizedBy("copyApkToGeneratedFolder")
}
