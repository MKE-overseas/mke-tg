import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maven-publish")
    kotlin("jvm") version "1.9.10"
}

group = "team.mke"
version = "1.0"

dependencies {
    implementation(libs.raysmith.tgBot)
    implementation(libs.raysmith.utils)
    implementation(libs.raysmith.google)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
}

tasks{
    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xcontext-receivers"
        }
    }
}
kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MKE-overseas/mke-tg")
            credentials {
                username = System.getenv("GIT_USERNAME")
                password = System.getenv("GIT_TOKEN_PUBLISH")
            }
        }
    }
}