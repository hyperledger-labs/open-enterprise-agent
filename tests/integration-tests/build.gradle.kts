plugins {
    kotlin("jvm") version "1.9.21"
    idea
    java
    id("net.serenity-bdd.serenity-gradle-plugin") version "4.0.46"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "io.iohk.atala.prism"
version = "1.0-SNAPSHOT"

buildscript {
    dependencies {
        classpath("net.serenity-bdd:serenity-single-page-report:4.1.4")
        classpath("net.serenity-bdd:serenity-json-summary-report:4.1.4")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/hyperledger-labs/open-enterprise-agent/")
        credentials {
            username = System.getenv("ATALA_GITHUB_ACTOR")
            password = System.getenv("ATALA_GITHUB_TOKEN")
        }
    }
}

dependencies {
    // HTTP listener
    testImplementation("io.ktor:ktor-server-netty:2.3.0")
    testImplementation("io.ktor:ktor-client-apache:2.3.0")
    // RestAPI client
    testImplementation("io.iohk.atala.prism:prism-kotlin-client:1.31.0")
    // Test helpers library
    testImplementation("io.iohk.atala:atala-automation:0.3.2")
    // Hoplite for configuration
    testImplementation("com.sksamuel.hoplite:hoplite-core:2.7.5")
    testImplementation("com.sksamuel.hoplite:hoplite-hocon:2.7.5")
    // Kotlin compose
    testImplementation("org.testcontainers:testcontainers:1.19.1")
}

serenity {
    reports = listOf("single-page-html", "json-summary")
}

tasks.register<Delete>("cleanTarget") {
    group = "verification"
    delete("target")
}

tasks.test {
    dependsOn("cleanTarget")
    finalizedBy("reports")
    testLogging.showStandardStreams = true
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags"))
}

kotlin {
    jvmToolchain(19)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.2.1")
}

/**
 * Creates a new entry in verification group for each conf file
 */
afterEvaluate {
    val folderPath = "src/test/resources/configs" // Change this to the path of your folder

    val configFiles = fileTree(folderPath)
        .matching { include("**/*.conf") }
        .map { it.name.replace(".conf", "") }
        .toList()

    configFiles.forEach { fileName ->
        tasks.register<Test>("test_$fileName") {
            group = "verification"
            testLogging.showStandardStreams = true
            systemProperty("TESTS_CONFIG", "/configs/$fileName.conf")
            systemProperty("PRISM_NODE_VERSION", System.getenv("PRISM_NODE_VERSION") ?: "")
            systemProperty("OPEN_ENTERPRISE_AGENT_VERSION", System.getenv("OPEN_ENTERPRISE_AGENT_VERSION") ?: "")
            systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags"))
            finalizedBy("aggregate", "reports")
        }
    }

    /**
     * Runs the integration suite for each config file present
     * Restrictions: aggregation of all executions doesn't work because of serenity configuration
     */
    tasks.register("regression") {
        dependsOn("cleanTarget")
        group = "verification"
        configFiles.forEach {
            dependsOn(tasks.getByName("test_$it"))
        }
    }
}
