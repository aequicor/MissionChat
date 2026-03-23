import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop") {
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.components.resources)
        }

        desktopMain.dependencies {
            implementation(projects.shared)
            implementation(projects.app.byCompose.common)

            //decompose
            implementation(libs.decompose)
            implementation(libs.decompose.compose)

            //coroutines swing module
            implementation(libs.kotlinx.coroutines.swing)

            //koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            //napier logger
            implementation(libs.napier)

            //compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            //   implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            includeAllModules = false
            modules = arrayListOf(
                "java.base",
                "java.desktop",
                "java.logging",
                "jdk.crypto.ec"
            )
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
                TargetFormat.Exe
            )
            packageName = "ru.kyamshanov.missionChat"
            packageVersion = "1.0.0"
            description = "missionChat"
            copyright = "© 2025 KYamshanov. All rights reserved."
            vendor = "KYamshanov"

            windows {
                menu = true
                iconFile.set(project.file("icons/app_icon.ico"))
            }
            appResourcesRootDir.set(project.layout.projectDirectory.dir("assets"))
            jvmArgs += "-splash:app/resources/splash_logo.png"
        }
        buildTypes.release.proguard {
            obfuscate.set(true)
            isEnabled.set(true)
            configurationFiles.from("proguard-rules.pro")
        }
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))
}