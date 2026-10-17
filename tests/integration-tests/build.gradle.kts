plugins {
    kotlin("jvm") version "2.1.20"
    idea
    java
    id("net.serenity-bdd.serenity-gradle-plugin") version "4.0.46"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "io.iohk.atala.prism"
version = "1.0-SNAPSHOT"

buildscript {
    dependencies {
        classpath("net.serenity-bdd:serenity-single-page-report:4.0.46")
        classpath("net.serenity-bdd:serenity-json-summary-report:4.0.46")
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

// Cloud-agent REST API client version.
// CI: the integration-tests workflow generates the client from the current OpenAPI
// specification, publishes it to mavenLocal, and passes it via -PagentClientVersion
// (ORG_GRADLE_PROJECT_agentClientVersion).
// Local dev: publish it yourself from cloud-agent/client/kotlin
// (`./gradlew publishToMavenLocal` publishes the default 0.0.1-SNAPSHOT).
val agentClientVersion =
    project.findProperty("agentClientVersion") as String? ?: "0.0.1-SNAPSHOT"

dependencies {
    // HTTP listener
    testImplementation("io.ktor:ktor-server-netty:2.3.0")
    testImplementation("io.ktor:ktor-client-apache:2.3.0")
    // RestAPI client
    testImplementation("org.hyperledger.identus:cloud-agent-client:$agentClientVersion")
    // Test helpers library
    testImplementation("io.iohk.atala:atala-automation:0.4.0")
    // Hoplite for configuration
    testImplementation("com.sksamuel.hoplite:hoplite-core:2.7.5")
    testImplementation("com.sksamuel.hoplite:hoplite-hocon:2.7.5")
    // Testcontainers (align all TC modules)
    testImplementation("org.testcontainers:testcontainers:2.0.3")
    // Crypto
    testImplementation("com.nimbusds:nimbus-jose-jwt:9.40")
    testImplementation("org.bouncycastle:bcprov-jdk18on:1.80")
    testImplementation("com.google.crypto.tink:tink:1.13.0")
    testImplementation("org.hyperledger.identus:apollo-jvm:1.8.8")
    // OID4VCI
    testImplementation("org.htmlunit:htmlunit:4.3.0")
    testImplementation("eu.europa.ec.eudi:eudi-lib-jvm-openid4vci-kt:0.4.1")
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
    finalizedBy("aggregate", "reports")
    jvmArgs("-Dlog4j2.disable.jmx=true")
    testLogging.showStandardStreams = true
    systemProperty("AGENT_VERSION", System.getenv("AGENT_VERSION") ?: "")
    systemProperty("PRISM_NODE_VERSION", System.getenv("PRISM_NODE_VERSION") ?: "")
    systemProperty("NEOPRISM_VERSION", System.getenv("NEOPRISM_VERSION") ?: "")
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags"))
    // Since the test runs on host and system-unter-test runs in containers,
    // We need to make the test on host resolves host.docker.internal same as the containerized services,
    // because some spec (e.g. OID4VCI) requires domain to be the same.
    //
    // The OID4VCI library does not allow mixing host.docker.internal and localhost
    systemProperty("jdk.net.hosts.file", "hosts_test")
}

kotlin {
    jvmToolchain(21)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.2.1")
}

configurations.all {
    resolutionStrategy {
        // Keep any transitive TC artifacts aligned to 2.0.3
        force("org.testcontainers:testcontainers:2.0.3")
    }
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
            jvmArgs("-Dlog4j2.disable.jmx=true")
            systemProperty("context", fileName)
            val mounted = file("/configs/$fileName.conf")
            val local = project.layout.projectDirectory.file("src/test/resources/configs/$fileName.conf").asFile
            val chosenConfig = if (mounted.exists()) mounted.absolutePath else local.absolutePath
            systemProperty("TESTS_CONFIG", chosenConfig)
            systemProperty("PRISM_NODE_VERSION", System.getenv("PRISM_NODE_VERSION") ?: "")
            systemProperty("NEOPRISM_VERSION", System.getenv("NEOPRISM_VERSION") ?: "")
            systemProperty("AGENT_VERSION", System.getenv("AGENT_VERSION") ?: "")
            systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags"))
            // Infer ledger driver from config name if not explicitly set
            val inferredDriver = when {
                fileName.contains("neoprism") -> "neoprism"
                fileName.contains("prism_node") || fileName == "basic" -> "prism-node"
                else -> ""
            }
            environment("VDR_LEDGER_DRIVER", System.getenv("VDR_LEDGER_DRIVER") ?: inferredDriver)
            systemProperty("jdk.net.hosts.file", "hosts_test")
            finalizedBy("aggregate", "reports")
            outputs.upToDateWhen { false }
        }
    }

    /**
     * Runs the integration suite for each config file present
     * Restrictions: aggregation of all executions doesn't work because of serenity configuration
     */
    tasks.register<Test>("regression") {
        dependsOn("cleanTarget")
        group = "verification"
        configFiles.forEach {
            dependsOn(tasks.getByName("test_$it"))
        }
    }
}
