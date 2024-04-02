import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0-Beta2"
    `maven-publish`
    signing
    alias(libs.plugins.nmcp)
}

group = "team.mke"
version = "1.2.0"

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
    publications {
        create<MavenPublication>("release") {
            artifactId = project.name
            groupId = project.group.toString()
            version = project.version.toString()
            from(components["java"])
            pom {
                packaging = "jar"
                name.set("Google")
                url.set("https://github.com/MKE-overseas/mke-tg")
                description.set("DSL wrappers for java google libs")



                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    connection.set("scm:https://github.com/MKE-overseas/mke-tg.git")
                    developerConnection.set("scm:git@github.com:MKE-overseas/mke-tg.git")
                    url.set("https://github.com/MKE-overseas/mke-tg")
                }

                developers {
                    developer {
                        id.set("RaySmith-ttc")
                        name.set("Ray Smith")
                        email.set("raysmith.ttcreate@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().matches(".*(SNAPSHOT|rc.\\d+)".toRegex())) snapshotsUrl else releasesUrl
            credentials {
                username = System.getenv("SONATYPE_USER")
                password = System.getenv("SONATYPE_PASS")
            }
        }
    }
}

nmcp {
    publish("release") {
        username.set(System.getenv("CENTRAL_SONATYPE_USER"))
        password.set(System.getenv("CENTRAL_SONATYPE_PASS"))
        publicationType.set("USER_MANAGED")
        publicationType.set("AUTOMATIC")
    }
}


signing {
    sign(configurations.archives.get())
    sign(publishing.publications["release"])
}