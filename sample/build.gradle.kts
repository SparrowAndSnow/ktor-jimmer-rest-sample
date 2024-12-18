plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
}

group = "com.example"
version = "0.0.1"



repositories {
    maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
    mavenCentral()
}

subprojects{
    apply(plugin = "kotlin")
    apply {
        plugin(rootProject.libs.plugins.ktor.get().pluginId)
        plugin(rootProject.libs.plugins.ksp.get().pluginId)
        plugin(rootProject.libs.plugins.kotlinx.serialization.get().pluginId)
    }
    dependencies {
        implementation("com.eimsound:ktor-jimmer-rest")
        ksp(rootProject.libs.jimmer.ksp)
        ksp(rootProject.libs.koin.annotations.ksp)
        implementation(rootProject.libs.bundles.api)
        testImplementation(rootProject.libs.bundles.api.test)
    }
}
