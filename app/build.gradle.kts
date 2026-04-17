plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.design.readerapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.design.readerapp"
        minSdk = 24
        targetSdk = 36

        // Versionado dinámico:
        // - En CI (GitHub Actions): valores inyectados desde el tag de Git.
        // - En local: usa los defaults ("1.0" y 1).
        versionName = System.getenv("VERSION_NAME") ?: "1.0"
        versionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Credenciales leídas desde variables de entorno (GitHub Secrets en CI).
            // En local estarán vacías y el APK saldrá sin firmar (solo con un warning).
            val keystorePath = System.getenv("KEYSTORE_PATH")
            val keystorePwd = System.getenv("KEYSTORE_PASSWORD")
            val alias = System.getenv("KEY_ALIAS")
            val keyPwd = System.getenv("KEY_PASSWORD")

            // Solo asignamos si TODAS las env vars existen.
            // Así el bloque es inerte en local (no crashea) y activo en CI.
            if (!keystorePath.isNullOrBlank() &&
                !keystorePwd.isNullOrBlank() &&
                !alias.isNullOrBlank() &&
                !keyPwd.isNullOrBlank()
            ) {
                storeFile = file(keystorePath)
                storePassword = keystorePwd
                keyAlias = alias
                keyPassword = keyPwd
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Asignación INCONDICIONAL. La lógica condicional vive dentro
            // del signingConfig, no acá. Así evitamos el bug del CI donde
            // el if no se evaluaba correctamente en tiempo de configuración.
            signingConfig = signingConfigs.getByName("release")
        }
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

    // Core Android / Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.foundation)

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")

    // 🔥 FIREBASE BOM (OBLIGATORIO)
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Firebase (SIN VERSIONES)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Google Login
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}