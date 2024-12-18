plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "sample"

includeBuild("../ktor-jimmer-rest/ktor-jimmer-rest")

dependencyResolutionManagement{
    repositories {
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        mavenCentral()
    }
}

include("book-service")
include("order-service")
