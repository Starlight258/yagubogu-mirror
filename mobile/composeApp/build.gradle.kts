import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.android.build.gradle.internal.dsl.SigningConfig
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.buildkonfig)

    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.oss.licenses.plugin)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

buildkonfig {
    packageName = "com.yagubogu"

    defaultConfigs {
        buildConfigField(
            STRING,
            "BASE_URL",
            "${gradleLocalProperties(rootDir, providers).getProperty("BASE_URL_DEBUG")}",
        )
        buildConfigField(
            type = STRING,
            "WEB_CLIENT_ID",
            "${gradleLocalProperties(rootDir, providers).getProperty("WEB_CLIENT_ID")}",
        )

        val fixedDate = gradleLocalProperties(rootDir, providers).getProperty("DEBUG_FIXED_DATE")
        if (fixedDate != null) {
            buildConfigField(STRING, "DEBUG_FIXED_DATE", "\"$fixedDate\"")
        } else {
            buildConfigField(STRING, "DEBUG_FIXED_DATE", "null")
        }
        buildConfigField(BOOLEAN, "IS_DEBUG", "true")
        buildConfigField(INT, "VERSION_CODE", "20201")
    }

    defaultConfigs("release") {
        buildConfigField(
            STRING,
            "BASE_URL",
            "${gradleLocalProperties(rootDir, providers).getProperty("BASE_URL_RELEASE")}",
        )
        buildConfigField(
            type = STRING,
            "WEB_CLIENT_ID",
            "${gradleLocalProperties(rootDir, providers).getProperty("WEB_CLIENT_ID")}",
        )

        buildConfigField(BOOLEAN, "IS_DEBUG", "false")
    }


}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // AndroidX Core
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.fragment.ktx)
            implementation(libs.androidx.datastore.preferences)

            // Firebase
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics.ndk)

            // Google Credentials
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.google.googleid)

            // Google Services
            implementation(libs.play.services.location)
            implementation(libs.play.services.oss.licenses)

            // Play In-App Update
            implementation(libs.app.update)
            implementation(libs.app.update.ktx)

//            // Testing
//            testImplementation(libs.junit)
//            testImplementation(libs.kotest.runner.junit5)
//            testImplementation(libs.kotest.assertions.core)
//            testImplementation(libs.kotlinx.coroutines.test)
//            androidTestImplementation(libs.androidx.junit)
//            androidTestImplementation(libs.androidx.espresso.core)
//            androidTestImplementation(project.dependencies.platform(libs.androidx.compose.bom))
//            androidTestImplementation(libs.androidx.ui.test.junit4)

            // Debug
//            debugImplementation(libs.androidx.ui.tooling)
//            debugImplementation(libs.androidx.ui.test.manifest)
        }
        commonMain.dependencies {
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // Kotlinx
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Ktor & Ktorfit
            implementation(libs.ktorfit.lib)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)

            // Compose
            implementation(libs.androidx.lifecycle.runtime.ktx)

            // Navigation3
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation3)

            // Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.okhttp)
            implementation(libs.imagepickerkmp)
            implementation(libs.compressor)

            // UI Components
            implementation(libs.material)
            implementation(libs.balloon.compose)
            implementation(libs.calendar.compose.multiplatform)

            // Logging
            implementation(libs.kermit)
            implementation(libs.kermit.crashlytics)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.yagubogu"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.yagubogu"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2_02_01
        versionName = "2.2.1"

        manifestPlaceholders["appName"] = "@string/app_name"
    }
    val signingFile = rootProject.file("keystore.properties")
    val releaseSigningConfig: SigningConfig? =
        if (signingFile.exists()) {
            val keystoreProperties =
                Properties().apply {
                    load(FileInputStream(signingFile))
                }

            signingConfigs.create("release") {
                storeFile = file("yagubogu-keystore")
                keyAlias = "${keystoreProperties["KEY_ALIAS"]}"
                keyPassword = "${keystoreProperties["KEY_PASSWORD"]}"
                storePassword = "${keystoreProperties["KEYSTORE_PASSWORD"]}"
            }
        } else {
            null
        }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
            manifestPlaceholders["appName"] = "야구보구.debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders["appName"] = "@string/app_name"
            if (releaseSigningConfig != null) {
                signingConfig = releaseSigningConfig
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    add("kspAndroid", "de.jensklingenberg.ktorfit:ktorfit-ksp:2.7.2")
}

