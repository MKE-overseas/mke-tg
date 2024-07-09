import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    signing
    alias(libs.plugins.nmcp)
    alias(libs.plugins.benManes.versions)
}

group = "team.mke"
version = "1.4.0"

dependencies {
    implementation(libs.raysmith.tgBot.jvm)
    implementation(libs.raysmith.utils)
    implementation(libs.raysmith.google)
    implementation(libs.mke.utils)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.google.apis.sheets)
}

tasks {
    test {
        useJUnitPlatform()
    }

    named<DependencyUpdatesTask>("dependencyUpdates").configure {
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val stableList = listOf("RELEASE", "FINAL", "GA")

        rejectVersionIf {
            val stableKeyword = stableList.any { candidate.version.uppercase().contains(it) }
            val isStable = stableKeyword || regex.matches(candidate.version)
            isStable.not()
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withJavadocJar()
    withSourcesJar()
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
                name.set("MKE tg utils")
                url.set("https://github.com/MKE-overseas/mke-tg")
                description.set("MKE features and utils for tg-bot lib")



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

signing {
    publishing.publications.forEach {
        sign(it)
    }
}

nmcp {
    publish("release") {
        username.set(System.getenv("CENTRAL_SONATYPE_USER"))
        password.set(System.getenv("CENTRAL_SONATYPE_PASS"))
        publicationType.set("AUTOMATIC")
    }
}


//signing {
//    sign(configurations.archives.get())
//    sign(publishing.publications["release"])
//}