plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // ... tus otros plugins
}

android {
    namespace = "com.tuapp.package"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tuapp.package"
        minSdk = 24
        targetSdk = 34

        // Versionado dinámico: lee del tag de Git en CI, usa default en local
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
        versionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Solo configura la firma si las variables de entorno existen (CI)
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Solo aplica la firma si está configurada
            if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ... tus dependencias
}