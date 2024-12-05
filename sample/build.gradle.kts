plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
    mavenCentral()
}

dependencies {
//    implementation(fileTree("../ktor-jimmer-rest/ktor-jimmer-rest/build/libs") { include("*.jar") })
    implementation("com.eimsound:ktor-jimmer-rest")
    ksp(libs.jimmer.ksp)
    ksp(libs.koin.annotations.ksp)
    implementation(libs.bundles.api)
    testImplementation(libs.bundles.api.test)
}
// 将生成的代码添加到编译路径中。
// 没有这个配置，gradle命令仍然可以正常执行，
// 但是, Intellij无法找到生成的源码。
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
