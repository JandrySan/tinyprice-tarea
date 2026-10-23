import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localConfig = Properties().apply {
    val configFile = rootProject.file("local.properties")
    if (configFile.isFile) configFile.inputStream().use { load(it) }
}
fun localConfigValue(name: String, fallback: String): String =
    localConfig.getProperty(name)?.takeIf { it.isNotBlank() } ?: fallback
fun supabaseConfigValue(uppercaseName: String, legacyName: String, fallback: String): String =
    localConfig.getProperty(uppercaseName)?.takeIf { it.isNotBlank() }
        ?: localConfigValue(legacyName, fallback)
fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val supabaseUrl = supabaseConfigValue(
    "SUPABASE_URL",
    "supabase.url",
    "https://YOUR_PROJECT_REF.supabase.co",
)
val supabaseAnonKey = supabaseConfigValue(
    "SUPABASE_ANON_KEY",
    "supabase.anonKey",
    "YOUR_SUPABASE_ANON_KEY",
)
val edgeRuntimeBaseUrl = localConfigValue(
    "edgeRuntimeBaseUrl",
    "$supabaseUrl/functions/v1",
)
val tinyFishEndpoint = localConfigValue(
    "tinyfish.endpoint",
    "https://agent.tinyfish.ai/v1/automation/run-sse",
)
val tinyFishApiKey = supabaseConfigValue(
    "TINYFISH_API_KEY",
    "tinyfish.apiKey",
    "YOUR_TINYFISH_API_KEY",
)
val useRemoteDeals = providers.gradleProperty("useRemoteDeals").orNull == "true"

android {
    namespace = "com.apps.iatinyfish"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.apps.iatinyfish"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // Values come from local.properties, which is excluded from version control.
        buildConfigField("String", "SUPABASE_URL", buildConfigString(supabaseUrl))
        buildConfigField("String", "SUPABASE_ANON_KEY", buildConfigString(supabaseAnonKey))
        buildConfigField("String", "EDGE_RUNTIME_BASE_URL", buildConfigString(edgeRuntimeBaseUrl))
        buildConfigField("String", "TINYFISH_ENDPOINT", buildConfigString(tinyFishEndpoint))
        buildConfigField("String", "TINYFISH_API_KEY", buildConfigString(tinyFishApiKey))
        // Keep preview data by default. Enable the hosted function with:
        // -PuseRemoteDeals=true
        buildConfigField("boolean", "USE_REMOTE_DEALS", useRemoteDeals.toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
