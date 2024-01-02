plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.uroria"
version = project.properties["projectVersion"].toString()

repositories {
    mavenCentral()
}

val jetbrainsAnnotationsVersion: String by project.extra
val lombokVersion: String by project.extra
val jsonVersion: String by project.extra
val fastUtilVersion: String by project.extra
dependencies {
    implementation("org.jetbrains:annotations:${jetbrainsAnnotationsVersion}")
    implementation("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.jetbrains:annotations:${jetbrainsAnnotationsVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    // Json
    implementation("org.json:json:${jsonVersion}")

    implementation("it.unimi.dsi:fastutil:${fastUtilVersion}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("${System.getenv("CI_API_V4_URL")}/projects/660/packages/maven")
            name = "GitLab"
            if (System.getenv("CI") == "true") {
                credentials(HttpHeaderCredentials::class) {
                    name = "Private-Token"
                    value = System.getenv("U_PIPELINE_DEFAULT_TOKEN")
                }
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}

tasks {
    shadowJar {
        minimize()
        relocate("org.json", "com.uroria.fastconfig.json")
        relocate("it.unimi.dsi", "com.uroria.fastconfig")

        archiveFileName.set("FastConfig-${project.version}")
    }
}
