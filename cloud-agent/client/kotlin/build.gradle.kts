plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "org.hyperledger.identus"

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.23")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

kotlin {
    jvmToolchain(11)
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(group.toString(), project.name, project.version.toString())
    pom {
        name.set("Hyperledger Identus Cloud Agent HTTP Client")
        description.set("The HTTP client for the Hyperledger Identus Cloud Agent generated from OpenAPI specification")
        url.set("https://identus.io/")
        organization {
            name.set("Hyperledger")
            url.set("https://hyperledger.org/")
        }
        issueManagement {
            system.set("Github")
            url.set("https://github.com/hyperledger-identus/cloud-agent")
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("hyperledger-identus")
                name.set("Hyperledger Identus contributors")
                url.set("https://github.com/hyperledger-identus/cloud-agent/graphs/contributors")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/hyperledger-identus/cloud-agent.git")
            developerConnection.set("scm:git:ssh://github.com/hyperledger-identus/cloud-agent.git")
            url.set("https://github.com/hyperledger-identus/cloud-agent")
        }
    }
}
