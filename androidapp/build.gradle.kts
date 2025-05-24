import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.parcelize)
}
val localProperties = Properties().apply {
    load(File(rootProject.rootDir, "local.properties").inputStream())
}

android {
    namespace = "org.dam.tfg.androidapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.dam.tfg.androidapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        localProperties["MONGODB_URI"]?.let {
            buildConfigField("String", "MONGODB_URI", "\"$it\"")
        }

        // Añadir BASE_URL desde local.properties si existe
        localProperties["BASE_URL"]?.let {
            buildConfigField("String", "BASE_URL", "\"$it\"")
        } ?: buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:27017/\"")

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            // Añade la ruta a la lista de exclusiones
            excludes.add("META-INF/native-image/native-image.properties")
            // o para el folder:
            excludes.add("META-INF/native-image/**")
            excludes += setOf(
                "javax/annotation/**",
                "javax/naming/**" // Excluir clases JNDI
            )
        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.mongodb.driver.kotlin.coroutine)

    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.mongodb.sync)
    implementation(libs.coil.compose.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.kotlinx.serialization.converter)
    implementation(libs.navigation.runtime.android)
    implementation(libs.bson.kotlinx)
    implementation(libs.java.jwt)
    implementation(libs.exp4j)
    implementation(libs.dnsjava)
}

