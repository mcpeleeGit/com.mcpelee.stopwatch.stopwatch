import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.artifact.SingleArtifact
import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import com.github.triplet.gradle.androidpublisher.ResolutionStrategy
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.play.publisher)
}

val playServiceAccountFilePath = providers.gradleProperty("PLAY_SERVICE_ACCOUNT_FILE")
    .orElse(providers.environmentVariable("PLAY_SERVICE_ACCOUNT_FILE"))
    .orNull
val hasInlinePlayCredentials = !providers.environmentVariable("ANDROID_PUBLISHER_CREDENTIALS").orNull.isNullOrBlank()
val hasPlayServiceAccountFile = !playServiceAccountFilePath.isNullOrBlank()

val releaseStoreFilePath = providers.gradleProperty("RELEASE_STORE_FILE")
    .orElse(providers.environmentVariable("RELEASE_STORE_FILE"))
    .orNull
val releaseStorePassword = providers.gradleProperty("RELEASE_STORE_PASSWORD")
    .orElse(providers.environmentVariable("RELEASE_STORE_PASSWORD"))
    .orNull
val releaseKeyAlias = providers.gradleProperty("RELEASE_KEY_ALIAS")
    .orElse(providers.environmentVariable("RELEASE_KEY_ALIAS"))
    .orNull
val releaseKeyPassword = providers.gradleProperty("RELEASE_KEY_PASSWORD")
    .orElse(providers.environmentVariable("RELEASE_KEY_PASSWORD"))
    .orNull
val hasReleaseSigningConfig = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.googsu.stopwatch"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.googsu.stopwatch"
        minSdk = 32
        targetSdk = 36
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = file(checkNotNull(releaseStoreFilePath))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "src/main/keepRules/rules.keep",
            )
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
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

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val taskName = "package${variant.name.replaceFirstChar { it.uppercase() }}NativeSymbolTables"
        val taskProvider = tasks.register<PackageNativeSymbolTablesTask>(taskName) {
            description = "Packages already-stripped native libs as Play Console symbol tables."
            mergedNativeLibs.set(variant.artifacts.get(SingleArtifact.MERGED_NATIVE_LIBS))
        }
        variant.artifacts
            .use(taskProvider)
            .wiredWith(PackageNativeSymbolTablesTask::outputDir)
            .toAppendTo(MultipleArtifact.NATIVE_SYMBOL_TABLES)
    }
}

play {
    enabled.set(hasInlinePlayCredentials || hasPlayServiceAccountFile)
    defaultToAppBundles.set(true)
    track.set("internal")
    releaseStatus.set(ReleaseStatus.DRAFT)
    resolutionStrategy.set(ResolutionStrategy.IGNORE)

    if (hasPlayServiceAccountFile) {
        serviceAccountCredentials.set(file(checkNotNull(playServiceAccountFilePath)))
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.core.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

@DisableCachingByDefault(because = "Copies native libraries without transformation.")
abstract class PackageNativeSymbolTablesTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mergedNativeLibs: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun packageSymbolTables() {
        val output = outputDir.get().asFile
        output.deleteRecursively()
        output.mkdirs()

        mergedNativeLibs.get().asFile.walkTopDown()
            .filter { it.isFile && it.extension == "so" }
            .forEach { nativeLib ->
                val abiDir = output.resolve(nativeLib.parentFile.name)
                abiDir.mkdirs()
                nativeLib.copyTo(abiDir.resolve("${nativeLib.name}.sym"), overwrite = true)
            }
    }
}
