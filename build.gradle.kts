plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish") // 用来推送到jitpack
}

group = "com.github.jitpack"
version = "1.0"

android {
    namespace = "com.android.apphelper2"
    compileSdk = Config.compileSdk
    defaultConfig {
        minSdk = Config.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(Libs.appcompat)
    implementation(Libs.core_ktx)
    implementation(Libs.constraintlayout)
    implementation(Libs.lifecycle_runtime_ktx)
    implementation(Libs.lifecycle_viewmodel_ktx)
    implementation(Libs.recyclerview)

    //协程依赖 - 基础
    implementation(Libs.kotlinx_coroutines_core)
    implementation(Libs.activity_ktx)
    implementation(Libs.fragment_ktx)
    implementation(Libs.material)

    // gson
    implementation(Libs.gson)
    implementation(Libs.dimens)
    implementation(Libs.jeromq)
    implementation(project(":common"))
}

val VERSION = latestGitTag().ifEmpty { Config.versionName }
/**
 * 获取 git 仓库中最新的 tag作为版本号
 */
fun latestGitTag(): String {
    val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start()
    return process.inputStream.bufferedReader()
        .use { bufferedReader ->
            bufferedReader.readText()
                .trim()
        }
}

publishing { // 发布配置
    publications {// 发布内容
        create<MavenPublication>("release") {// 注册一个名字为 release 的发布内容
            groupId = "com.android.apphelper"
            artifactId = "appheler2"// 插件名称
            version = VERSION // 版本号
            afterEvaluate {// 在所有的配置都完成之后执行
                // 从当前 module 的 release 包中发布
                from(components["release"])
            }
        }
    }
}