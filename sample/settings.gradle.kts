plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "sample"

dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
//        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        mavenCentral()
    }
}

include("book-service")
include("order-service")

includeBuild("../ktor-jimmer-rest/ktor-jimmer-rest")
