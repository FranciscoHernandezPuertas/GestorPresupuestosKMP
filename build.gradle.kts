plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kobweb.library) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.ksp) apply false // Añadir esta línea si no está
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

subprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
