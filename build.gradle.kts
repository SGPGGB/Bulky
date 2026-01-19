var isJenkins = System.getenv("JENKINS_URL") == "https://jenkins.sgpggb.de/"

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.rosewooddev.io/repository/public/")
    }

    maven {
        name = "sgpggbRepo"
        url = uri("https://repo.sgpggb.de/repository/maven-releases/")
        credentials(PasswordCredentials::class)
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
    compileOnly("de.sgpggb:PluginUtilitiesLib:5.17")
    compileOnly("de.sgpggb:SGPGGBEconomySpigot:2.16")
    compileOnly("de.sgpggb:PluginChannel:1.7")
}

group = "de.sgpggb"
version = "1.3"
var mcapi = "1.21.10"
description "Bulky"

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    compileTestJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    processResources {
        val username = providers.systemProperty("user.name").get()
        var versionString = project.version
        inputs.property("finalversion", versionString)
        filesMatching("plugin.yml") {
            versionString = "${project.version}-local-${username} (${mcapi})"
            expand("finalversion" to versionString,
                "mcapi" to mcapi) {
                escapeBackslash = true
            }
        }
    }

    jar {
        archiveFileName.set("${project.name}.jar")
    }
}

if (isJenkins) {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }

        repositories {
            maven {
                name = "sgpggbRepo"
                url = uri("https://repo.sgpggb.de/repository/maven-releases/")
                credentials(PasswordCredentials::class)
            }
        }
    }
} else {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}