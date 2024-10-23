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

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenRaySmith("tg-bot")
        mavenLocal()
    }

    versionCatalogs {

    }
}