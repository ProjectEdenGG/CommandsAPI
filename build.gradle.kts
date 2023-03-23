plugins {
    java
    `maven-publish`
    id("io.freefair.lombok") version "8.0.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "${project.group}"
version = "${project.version}"

repositories {
    mavenLocal()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    implementation("org.objenesis:objenesis:3.2")
    implementation("io.github.classgraph:classgraph:4.8.138")
}

tasks {

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
        options.compilerArgs.add("-parameters")
    }

    javadoc { options.encoding = Charsets.UTF_8.name() }

    shadowJar {
        relocate("gg.projecteden.api", "commands.gg.projecteden.api")
        relocate("org.objenesis", "commands.org.objenesis")
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }

    repositories {
        maven {
            name = "edenSnapshots"
            url = uri("https://sonatype.projecteden.gg/repository/maven-snapshots/")
            credentials(PasswordCredentials::class)
        }
    }
}

