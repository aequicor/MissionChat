import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    applyDefaultHierarchyTemplate()
    jvm()
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {

        commonMain.dependencies {
            //module with logic
            implementation(projects.shared)

            //decompose
            implementation(libs.decompose)
            implementation(libs.decompose.compose)

            //flowMVI
            implementation(libs.flowmvi.essenty)
            implementation(libs.flowmvi.essenty.compose)

            //coroutines
            implementation(libs.kotlinx.coroutines.core)

            //koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            //immutable collections
            implementation(libs.kotlinx.immutable)

            //date time
            implementation(libs.kotlinx.datetime)

            //compose markdown renderer
            implementation(libs.multiplatform.markdown.renderer)
            implementation(libs.multiplatform.markdown.renderer.material3)

            //compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.uiToolingPreview)

            //project modules
            implementation(projects.app.byCompose.theme)
            implementation(projects.app.byCompose.uikit)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "ru.kyamshanov.missionChat"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))
    val metricsDir = project.layout.buildDirectory.dir("compose_metrics")
    metricsDestination.set(metricsDir)
    reportsDestination.set(metricsDir)
}