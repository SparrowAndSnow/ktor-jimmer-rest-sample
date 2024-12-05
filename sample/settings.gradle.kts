plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "sample"

includeBuild("../ktor-jimmer-rest/ktor-jimmer-rest")

//dependencyResolutionManagement {
//    project(":ktor-jimmer-rest")
//        .projectDir =
//        file("../ktor-jimmer-rest/ktor-jimmer-rest")
//}

