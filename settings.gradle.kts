import org.tomlj.Toml

rootProject.name = "mke-tg"

fun RepositoryHandler.mavenRaySmith(name: String) {
    maven {
        url = uri("https://maven.pkg.github.com/raysmith-ttc/$name")
        credentials {
            username = System.getenv("GIT_USERNAME")
            password = System.getenv("GIT_TOKEN_READ")
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.tomlj:tomlj:1.1.1")
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenRaySmith("tg-bot")
        mavenLocal()
    }

    versionCatalogs {
        create("mke") {
            val version = Toml
                .parse(file("gradle/libs.versions.toml").readText())
                .getString("versions.mke-utils")
                ?: error("Version 'mke-utils' not found in libs.versions.toml")

            from("team.mke:mke-utils-catalog:$version")
        }
    }
}